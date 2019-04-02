package pw.aru.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEventVO
import ch.qos.logback.core.AppenderBase
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.MessageOptions
import com.mewna.catnip.rest.handler.RestWebhook
import org.slf4j.LoggerFactory
import pw.aru.core.reporting.ErrorReporter
import pw.aru.utils.text.BOOK
import pw.aru.utils.text.webhook_pattern
import java.lang.Thread.interrupted
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.concurrent.thread

class DiscordLogBack : AppenderBase<ILoggingEvent>() {
    private lateinit var discordLayout: PatternLayout
    private lateinit var logLayout: PatternLayout

    @Synchronized
    private fun startThread(rest: RestWebhook, id: String, token: String) {
        if (thread != null) {
            throw IllegalStateException("Sender Thread already running")
        }

        val format by lazy { SimpleDateFormat("HH:mm:ss") }

        thread = thread(name = "DiscordLogBack Sender") {
            try {
                while (!interrupted()) {

                    // Get First (Blocking)
                    val first: ILoggingEvent = queue.take()
                    val list = arrayListOf(first)
                    val log = StringJoiner("\n").add(discordLayout.doLayout(first).trim())

                    val start = System.currentTimeMillis()
                    while (true) {
                        val event = queue.poll(700, TimeUnit.MILLISECONDS) ?: break
                        list.add(event)
                        log.add(discordLayout.doLayout(event).trim())
                        if (System.currentTimeMillis() > start + 1000) break
                    }



                    if (log.length() > 8000) {
                        rest.executeWebhook(
                            id, token,
                            MessageOptions()
                                .content("[`${format.format(Date())}`] [$BOOK] Log got too long: ${saveLogHtml(list)}")
                        )
                    } else {
                        val messages = LinkedList<String>()

                        var s = log.toString()

                        while (s.length > 2000) {
                            val i = s.lastIndexOfAny(charArrayOf(' ', '\n'), 2000)

                            if (i != -1) {
                                messages += s.substring(0, i).trim()
                                s = s.substring(i + 1).trim()
                            }
                        }

                        messages += s

                        messages.forEach {
                            rest.executeWebhook(
                                id, token,
                                MessageOptions().content(it)
                            )
                        }
                    }
                }
            } catch (e: InterruptedException) {
            }
        }
    }

    private fun saveLogHtml(logs: List<ILoggingEvent>): String {
        val mdc = logs.flatMap { it.mdcPropertyMap.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, v) -> v.singleOrNull() ?: v.toSortedSet() }

        val log = logs.joinToString("\n") { logLayout.doLayout(it).trim() }

        return ErrorReporter()
            .log(log)
            .extra("mdc", mdc)
            .extra("logCount", logs.size)
            .report()
            .logToFileAndGetUrl()
    }

    override fun append(event: ILoggingEvent) {
        if (event.level.isGreaterOrEqual(Level.INFO)) {
            synchronized(queue) {
                queue.offer(LoggingEventVO.build(event))
            }
        }
    }

    override fun start() {
        if (instance != null && instance != this) {
            return
        }

        instance = this

        discordLayout = PatternLayout()
        logLayout = PatternLayout()

        //Configuration
        discordLayout.context = this.context
        discordLayout.pattern = "[`%d{HH:mm:ss}`] [`%t/%level`] [`%logger{0}`]: %msg%n%ex{5}"
        discordLayout.start()

        logLayout.context = this.context
        logLayout.pattern = "[%d] [%t/%level] [%logger{1}]: %msg%n"
        logLayout.start()

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

        fun enable(catnip: Catnip, logWebhook: String) {
            if (instance == null) {
                LoggerFactory.getLogger(DiscordLogBack::class.java)
                    .error("Logback wasn't initialized; Attempting to boot it...")
            }

            if (instance == null) {
                throw IllegalStateException("DiscordLogBack instance not initialized.")
            }

            val (id, token) = Pattern.compile(webhook_pattern).matcher(logWebhook)
                .apply { if (!matches()) throw IllegalArgumentException("Failed to parse webhook URL") }
                .run { group(1) to group(2) }


            instance!!.startThread(catnip.rest().webhook(), id, token)
        }
    }
}