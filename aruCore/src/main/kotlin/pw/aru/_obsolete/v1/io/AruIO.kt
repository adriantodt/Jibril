package pw.aru._obsolete.v1.io

import io.lettuce.core.pubsub.RedisPubSubAdapter
import mu.KLogging
import org.json.JSONObject
import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.io.dsl.AruIOConfigurator
import pw.aru._obsolete.v1.io.entities.CallResponse
import pw.aru._obsolete.v1.io.entities.CommandCall
import pw.aru._obsolete.v1.io.entities.CommandResult
import pw.aru._obsolete.v1.io.entities.FeedMessage
import pw.aru.core.AruSide
import pw.aru.lib.eventpipes.EventPipes.newAsyncPipe
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import pw.aru.utils.extensions.lib.jsonStringOf
import java.io.Closeable
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors.newSingleThreadExecutor

class AruIO(val db: AruDB) : Closeable {
    companion object : KLogging() {
        private val EMPTY = JSONObject()
    }

    private var receiveFeeds = false
    private val feedHandlers = CopyOnWriteArrayList<FeedConsumer>()
    private var receiveCommands = false
    private val commandHandlers = CopyOnWriteArrayList<CallHandler>()

    private val feedWhiteList = CopyOnWriteArraySet<String>()
    private val feedPipeExecutor by lazy { newSingleThreadExecutor(threadGroupBasedFactory("AruIO-feedPipeExecutor")) }
    private val feedPipe by lazy { newAsyncPipe<FeedMessage>(feedPipeExecutor) }
    private val commandPipeExecutor by lazy { newSingleThreadExecutor(threadGroupBasedFactory("AruIO-commandPipeExecutor")) }
    private val commandPipe by lazy { newAsyncPipe<CommandCall>(commandPipeExecutor) }
    private val subConnection by lazy { db.client.connectPubSub() }

    override fun close() {
        logger.trace { "aruIO received close, cleaning up..." }

        if (receiveFeeds) {
            feedWhiteList.clear()
            feedHandlers.clear()
            feedPipe.close()
            feedPipeExecutor.shutdown()
        }

        if (receiveCommands) {
            commandHandlers.clear()
            commandPipe.close()
            commandPipeExecutor.shutdown()
        }

        if (receiveFeeds || receiveCommands) {
            subConnection.close()
            receiveFeeds = false
            receiveCommands = false
        }
    }

    fun configure(block: AruIOConfigurator.() -> Unit) {
        val (sidesRegistered, feedsRegistered, commandsRegistered) = AruIOConfigurator().also(block)

        if (feedsRegistered.isNotEmpty()) {
            feedHandlers += feedsRegistered

            if (sidesRegistered.isEmpty()) {
                listenFeeds(*AruSide.values())
            } else {
                listenFeeds(*sidesRegistered.toTypedArray())
            }
        }

        if (commandsRegistered.isNotEmpty()) {
            commandHandlers += commandsRegistered

            listenCommands()
        }
    }

    fun listenFeeds(vararg targets: AruSide) {
        val setupFeeds = !receiveFeeds
        receiveFeeds = true

        if (setupFeeds) setupFeedHandle()

        val channels = targets.map { "${it.moduleName}:channel.feed" }
        feedWhiteList += channels
        logger.trace { "listening to $channels for feeds" }
        subConnection.sync().subscribe(*channels.toTypedArray())
    }

    fun listenCommands() {
        val setupCommands = !receiveCommands
        receiveCommands = true

        if (setupCommands) setupCommandHandle()

        logger.trace { "listening for commands" }
        subConnection.sync().subscribe("${db.side.moduleName}:channel.input")
    }

    fun addFeedConsumer(handler: FeedConsumer) {
        feedHandlers += handler
    }

    fun addCallHandler(handler: CallHandler) {
        commandHandlers += handler
    }

    fun sendCommand(target: AruSide, method: String, data: JSONObject = EMPTY): CommandResult {
        logger.trace { "sending command to $target | raw is $method, $data" }
        val inputChannel = "${target.moduleName}:channel.input"
        val outputChannel = "${target.moduleName}:channel.output"
        val cmdId = UUID.randomUUID().toString()


        val nextMessages = subConnection.reactive().observeChannels()
            .filter { it.channel == outputChannel }
            .map { it.message.runCatching(::JSONObject).getOrNull() }
            .filter { it?.optString("id") == cmdId }
            .cache()

        db.conn.sync().publish(
            inputChannel,
            jsonStringOf(
                "source" to db.side,
                "id" to cmdId,
                "method" to method,
                "data" to data
            )
        )

        val output = runCatching { nextMessages.blockFirst(Duration.ofSeconds(2)) }.getOrNull()

        return when {
            output == null -> CommandResult.Timeout
            output.has("err") -> output.getJSONObject("err").let {
                CommandResult.Error(
                    it.getString("type"),
                    it.optString("message"),
                    it.optJSONObject("data")
                )
            }
            output.has("data") -> CommandResult.Successful(output.getJSONObject("data"))
            else -> CommandResult.EmptyResponse
        }
    }

    fun sendFeed(type: String, data: JSONObject = EMPTY) {
        logger.trace { "sending feed | raw is $type, $data" }
        db.conn.sync().publish(
            "${db.side.moduleName}:channel.feed",
            jsonStringOf(
                "type" to type,
                "data" to data
            )
        )
    }

    private fun setupFeedHandle() {
        logger.trace { "setting up feed handle" }

        feedPipe.subscribe {
            for (handler in feedHandlers) {
                it.runCatching(handler)
            }
        }

        subConnection.addListener(
            object : RedisPubSubAdapter<String, String>() {
                override fun message(channel: String, message: String) {
                    if (channel in feedWhiteList) {
                        logger.trace { "new feed received | raw is $channel, $message" }
                        val j = message.runCatching { JSONObject(message) }.getOrNull() ?: return

                        val moduleName = channel.splitToSequence(':').first()
                        AruSide.values().first { it.moduleName == moduleName }

                        feedPipe.publish(
                            FeedMessage(
                                AruSide.values().first { it.moduleName == moduleName },
                                j.optString("type") ?: return,
                                j.optJSONObject("data") ?: return
                            )
                        )
                    }
                }
            }
        )
    }

    private fun setupCommandHandle() {
        logger.trace { "setting up command handle" }
        commandPipe.subscribe {
            for (handler in commandHandlers) {
                val response = it.runCatching(handler).getOrNull()

                if (response != null) {
                    answerCommand(it, response)
                    return@subscribe
                }
            }

            answerCommandNoHandle(it)
        }

        subConnection.addListener(
            object : RedisPubSubAdapter<String, String>() {
                private val ourChannel = "${db.side.moduleName}:channel.input"
                override fun message(channel: String, message: String) {
                    if (channel != ourChannel) return
                    logger.trace { "new command received | raw is $channel, $message" }
                    val j = message.runCatching { JSONObject(message) }.getOrNull() ?: return

                    commandPipe.publish(
                        CommandCall(
                            j.optEnum(AruSide::class.java, "source") ?: return,
                            j.optString("id") ?: return,
                            j.optString("method") ?: return,
                            j.optJSONObject("data") ?: return
                        )
                    )
                }
            }
        )
    }

    private fun answerCommand(call: CommandCall, response: CallResponse) {
        val j = JSONObject()
        j.put("id", call.callId)

        when (response) {
            is CallResponse.Success -> {
                response.data?.let {
                    j.put("data", it)
                }
            }
            is CallResponse.Error -> {
                val err = JSONObject().put("type", response.type)

                response.message?.let { err.put("message", it) }
                response.data?.let { err.put("data", it) }

                j.put("err", err)
            }
        }

        db.conn.async().publish("${db.side.moduleName}:channel.input", j.toString())
    }

    private fun answerCommandNoHandle(call: CommandCall) {
        answerCommand(call, CallResponse.Error("unhandledCall", null, null))
    }
}