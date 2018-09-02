package pw.aru.core.reporting

import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.Aru.errorQuotes
import pw.aru.core.commands.help.prefix
import pw.aru.db.AruDB
import pw.aru.snow64.Snow64
import pw.aru.utils.emotes.PENCIL
import pw.aru.utils.emotes.TALKING
import pw.aru.utils.emotes.WORRIED
import pw.aru.utils.extensions.*
import java.io.File
import java.lang.System.currentTimeMillis
import java.util.*

class ErrorReportHandler internal constructor(private val report: ErrorReport) {
    companion object {
        val fileWorker = Snow64.convert(AruDB.generator).getWorker(0, 1)
    }

    private var reportUrl: String? = null

    fun logToFile() = applyOn(report) {
        val fileId = fileWorker.generate()

        File("reports").mkdirs()

        val log = when {
            log != null && throwable == null -> log

            log == null && throwable != null && underlyingThrowable == null -> throwable.stackTraceToString().trim()

            log == null && throwable != null && underlyingThrowable != null -> throwable.stackTraceToString().trim() + "\n\nUnderlying Exception:\n" + underlyingThrowable.stackTraceToString().trim()

            else -> StringBuilder().apply {
                if (log != null) append("\nLog:\n").append(log.trim()).append("\n")
                if (throwable != null) {
                    append("\nException:\n").append(throwable.stackTraceToString().trim()).append("\n")
                    if (underlyingThrowable != null) {
                        append("\nUnderlying Exception:\n").append(throwable.stackTraceToString().trim()).append("\n")
                    }
                }
            }.trim().toString()
        }

        val extra = StringBuilder().apply {
            // errorId: String?
            if (errorId != null)
                append("ErrorID: ").append(errorId).append("\n")

            // command: ICommand?
            if (command != null)
                append("Command: ").append(command).append(" (").append(command.javaClass).append(")\n")

            // throwable: Throwable?
            if (throwable != null)
                append("Throwable: ").append(throwable.javaClass.name).append("\n")

            // underlyingThrowable: Throwable?
            if (underlyingThrowable != null)
                append("Underlying Throwable: ").append(underlyingThrowable.javaClass.name).append("\n")

            // channel: TextChannel?
            if (channel != null)
                append("TextChannel: ").append(channel).append("\n")

            // guild: Guild?
            if (guild != null)
                append("Guild: ").append(guild).append("\n")

            // member: Member?
            if (member != null)
                append("Member: ").append(member).append("\n")

            // message: Message?
            if (message != null)
                append("Message: ").append(message.contentRaw).append("\n")

            // extra: Map<String, Any?>
            if (extra != null)
                append("\nExtra: ").append(extra.toPrettyString(4)).append("\n")
        }.trim().toString()

        File("reports/$fileId.html").writeText(
            File("assets/aru/templates/logs.html").readText().replaceEach(
                "{date}" to Date(timestamp ?: currentTimeMillis()).toString(),
                "{log}" to log,
                "{extra}" to extra
            )
        )

        reportUrl = "https://reports.aru.pw/$fileId.html"
    }

    fun logToFileAndGetUrl() = logToFile().reportUrl!!

    fun logAsError() = applyOn(report) {
        fun errorLogString() = StringBuilder("**ERROR REPORTED**\n").apply {
            // errorId: String?
            if (errorId != null)
                append("**ErrorID**: ").append(errorId).append("\n")

            // command: ICommand?
            if (command != null)
                append("**Command**: ").append(command).append(" (").append(command.javaClass).append(")\n")

            // throwable: Throwable?
            if (throwable != null)
                append("**Throwable**: ").append(throwable.javaClass.name).append("\n")

            // underlyingThrowable: Throwable?
            if (underlyingThrowable != null)
                append("**Underlying Throwable**: ").append(underlyingThrowable.javaClass.name).append("\n")

            // channel: TextChannel?
            if (channel != null)
                append("**TextChannel**: ").append(channel).append("\n")

            // guild: Guild?
            if (guild != null)
                append("**Guild**: ").append(guild).append("\n")

            // member: Member?
            if (member != null)
                append("**Member**: ").append(member).append("\n")

            // message: Message?
            if (message != null)
                append("**Message**: ").append(message.contentRaw).append("\n")

            if (reportUrl != null)
                append("**Report**: ").append(reportUrl).append("\n")

            // extra: Map<String, Any?>
            if (extra != null)
                append("\n**Extra**: ").append(extra.toPrettyString(4)).append("\n")

        }.trim().toString()

        if (throwable != null) {
            ErrorReporter.logger.error(throwable, ::errorLogString)
        } else {
            ErrorReporter.logger.error(::errorLogString)
        }
    }

    fun sendErrorMessage() = sendErrorMessage(report.channel!!)

    fun sendErrorMessage(event: GuildMessageReceivedEvent) = sendErrorMessage(event.channel)

    fun sendErrorMessage(channel: TextChannel) = applyOn(report) {
        channel.sendMessage(
            "$WORRIED ${errorQuotes.random()}\n\n$TALKING Eh, do you mind reporting this to my developers? (Check out `$prefix${"hangout"}`)\n$PENCIL **Error ID**: `$errorId`"
        ).queue()
    }
}