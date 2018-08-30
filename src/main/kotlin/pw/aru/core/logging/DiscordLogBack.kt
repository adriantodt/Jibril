package pw.aru.core.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import pw.aru.db.AruDB
import pw.aru.snow64.Snow64
import pw.aru.utils.emotes.BOOK
import pw.aru.utils.extensions.classOf
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toPrettyString
import java.io.File
import java.lang.Thread.interrupted
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DiscordLogBack : AppenderBase<ILoggingEvent>() {
    private lateinit var discordLayout: PatternLayout
    private lateinit var logLayout: PatternLayout
    private val httpClient = OkHttpClient()

    @Synchronized
    private fun boot(client: WebhookClient) {
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

                    val joiner = StringJoiner("\n")
                    joiner.add(discordLayout.doLayout(first).trim())

                    val start = System.currentTimeMillis()
                    while (true) {
                        val event = queue.poll(700, TimeUnit.MILLISECONDS) ?: break
                        list += event
                        joiner.add(discordLayout.doLayout(event).trim())
                        if (System.currentTimeMillis() > start + 1000) break
                    }

                    if (joiner.length() > 8000) {
                        client.send("[`${format.format(Date())}`] [$BOOK] Log got too long: ${saveLogHtml(list)}")
                    } else {
                        MessageBuilder().append(joiner.toString()).buildAll(SplitPolicy.SPACE).forEach { client.send(it) }
                    }
                }
            } catch (e: InterruptedException) {
            }

            client.close()
        }
    }

    private fun saveLogHtml(logs: List<ILoggingEvent>): String {
        val fileId = logWorker.generate()
        File("reports").mkdirs()

        val mdc = logs.flatMap { it.mdcPropertyMap.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, v) -> v.singleOrNull() ?: v.toSortedSet() }
            .toPrettyString(4)

        File("reports/$fileId.html").writeText(
            File("assets/aru/log.html").readText().replaceEach(
                "{date}" to Date().toString(),
                "{log}" to logs.joinToString("\n") { logLayout.doLayout(it).trim() },
                "{extra}" to "Log Count: ${logs.size}\n\nMDC:\n$mdc"
            )
        )

        return "https://reports.aru.pw/$fileId.html"
    }

    override fun append(event: ILoggingEvent) {
        if (event.level.isGreaterOrEqual(Level.INFO)) {
            synchronized(queue) { queue.offer(event) }
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
        val logWorker = Snow64.convert(AruDB.generator).getWorker(0, 1)

        private val queue = LinkedBlockingQueue<ILoggingEvent>()
        private var instance: DiscordLogBack? = null
        private var thread: Thread? = null

        fun disable() {
            if (thread != null) {
                thread!!.interrupt()
            }

            queue.clear()
        }

        fun enable(logWebhook: String) {
            if (instance == null) {
                LoggerFactory.getLogger(classOf<DiscordLogBack>()).error("Logback wasn't initialized; Attempting to boot it...")
            }

            if (instance == null) {
                throw IllegalStateException("DiscordLogBack instance not initialized.")
            }

            instance!!.boot(WebhookClientBuilder(logWebhook).build())
        }
    }
}