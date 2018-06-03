package pw.aru.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy
import net.dv8tion.jda.core.entities.MessageChannel
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import pw.aru.utils.emotes.BOOK
import pw.aru.utils.extensions.classOf
import pw.aru.utils.paste
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DiscordLogBack : AppenderBase<ILoggingEvent>() {
    private var layout: PatternLayout? = null
    private val httpClient = OkHttpClient()

    @Synchronized
    private fun boot(channel: MessageChannel) {
        if (thread != null) {
            throw IllegalStateException("Sender Thread already running")
        }

        val format by lazy { SimpleDateFormat("HH:mm:ss") }

        thread = thread(name = "DiscordLogBack Sender") {
            while (true) {
                // Get First
                val first: ILoggingEvent = queue.take()
                val joiner = StringJoiner("\n")

                joiner.add(layout!!.doLayout(first).trim())

                val start = System.currentTimeMillis()
                while (true) {
                    val events = queue.poll(500, TimeUnit.MILLISECONDS) ?: break
                    joiner.add(layout!!.doLayout(events).trim())
                    if (System.currentTimeMillis() > start + 1000) break
                }

                val log = joiner.toString()

                if (log.length > 8000) {
                    channel.sendMessage("[`${format.format(Date())}`] [$BOOK] Log got too long: ${paste(httpClient, log)}").queue()
                } else {
                    MessageBuilder().append(log)
                        .buildAll(SplitPolicy.SPACE)
                        .forEach { channel.sendMessage(it).queue() }
                }
            }
        }
    }

    override fun append(event: ILoggingEvent) {
        if (event.level.isGreaterOrEqual(Level.INFO)) {
            synchronized(queue) {
                queue.offer(event)
            }
        }
    }

    override fun start() {
        if (instance != null && instance != this) {
            return
        }

        instance = this

        layout = PatternLayout()

        //Configuration
        layout!!.context = this.context
        layout!!.pattern = "[`%d{HH:mm:ss}`] [`%t/%level`] [`%logger{0}`]: %msg%n%ex{5}"
        layout!!.start()

        super.start()
    }

    companion object {
        private val queue = LinkedBlockingQueue<ILoggingEvent>()
        private var instance: DiscordLogBack? = null
        private var thread: Thread? = null

        fun disable() {
            if (thread != null) {
                thread!!.interrupt()
            }

            queue.clear()
        }

        fun enable(channel: MessageChannel) {
            if (instance == null) {
                LoggerFactory.getLogger(classOf<DiscordLogBack>()).error("Logback wasn't initialized; Attempting to boot it...")
            }

            if (instance == null) {
                throw IllegalStateException("DiscordLogBack instance not initialized.")
            }

            instance!!.boot(channel)
        }
    }
}