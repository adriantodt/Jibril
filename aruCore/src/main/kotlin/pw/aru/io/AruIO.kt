package pw.aru.io

import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.json.JSONObject
import pw.aru.db.AruDB
import pw.aru.io.dsl.AruIOConfigurator
import pw.aru.io.entities.CallResponse
import pw.aru.io.entities.CommandCall
import pw.aru.io.entities.CommandResult
import pw.aru.io.entities.FeedMessage
import pw.aru.lib.eventpipes.EventPipes.newAsyncPipe
import pw.aru.sides.AruSide
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import pw.aru.utils.extensions.lib.jsonStringOf
import java.io.Closeable
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors.newSingleThreadExecutor

class AruIO(val db: AruDB) : Closeable {
    private var receiveFeeds = false
    private val feedHandlers = CopyOnWriteArrayList<FeedConsumer>()
    private var receiveCommands = false
    private val commandHandlers = CopyOnWriteArrayList<CallHandler>()

    private val feedPipeExecutor by lazy { newSingleThreadExecutor(threadGroupBasedFactory("AruIO-feedPipeExecutor")) }
    private val feedPipe by lazy { newAsyncPipe<FeedMessage>(feedPipeExecutor) }
    private val feedWhiteList = CopyOnWriteArraySet<String>()
    private val commandPipeExecutor by lazy { newSingleThreadExecutor(threadGroupBasedFactory("AruIO-commandPipeExecutor")) }
    private val commandPipe by lazy { newAsyncPipe<CommandCall>(commandPipeExecutor) }
    private val commandWhiteList = CopyOnWriteArraySet<String>()
    private val subConnection by lazy { db.client.connectPubSub() }

    override fun close() {
        if (receiveFeeds) {
            feedHandlers.clear()
            feedPipe.close()
            feedPipeExecutor.shutdown()
            feedWhiteList.clear()
        }

        if (receiveCommands) {
            commandHandlers.clear()
            commandPipe.close()
            commandPipeExecutor.shutdown()
            commandWhiteList.clear()
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

        subConnection.sync().subscribe(*targets.map { "${it.moduleName}:channel.feed" }.toTypedArray())
    }

    fun listenCommands() {
        val setupCommands = !receiveCommands
        receiveCommands = true

        if (setupCommands) setupCommandHandle()

        subConnection.sync().subscribe("${db.side.moduleName}:channel.input")
    }

    fun addFeedConsumer(handler: FeedConsumer) {
        feedHandlers += handler
    }

    fun addCallHandler(handler: CallHandler) {
        commandHandlers += handler
    }

    fun sendCommand(target: AruSide, method: String, data: JSONObject): CommandResult {
        val inputChannel = "${target.moduleName}:channel.input"
        val outputChannel = "${target.moduleName}:channel.output"
        val cmdId = UUID.randomUUID().toString()


        val nextMessages = subConnection.reactive().observeChannels()
            .filter { it.channel == outputChannel }
            .map { it.message.runCatching(::JSONObject).getOrNull() }
            .filter { it?.optString("id") == cmdId }

        db.conn.async().publish(
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

    fun sendFeed(type: String, data: JSONObject) {
        db.conn.async().publish(
            "${db.side.moduleName}:feed.secondary",
            jsonStringOf(
                "type" to type,
                "data" to data
            )
        )
    }

    private fun setupFeedHandle() {
        feedPipe.subscribe {
            for (handler in feedHandlers) {
                it.runCatching(handler)
            }
        }

        subConnection.addListener(
            object : RedisPubSubAdapter<String, String>() {
                override fun message(channel: String, message: String) {
                    if (channel in feedWhiteList) {
                        val j = message.runCatching { JSONObject(message) }.getOrNull() ?: return

                        feedPipe.publish(
                            FeedMessage(
                                j.optEnum(AruSide::class.java, "source") ?: return,
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
                override fun message(channel: String, message: String) {
                    if (channel in commandWhiteList) {
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