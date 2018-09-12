package pw.aru.core.reporting

import mu.KLogging
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.utils.MiscUtil
import org.slf4j.MDC
import pw.aru.core.commands.ICommand
import pw.aru.snow64.Snow64
import pw.aru.snowflake.SnowflakeConfig
import pw.aru.utils.extensions.applyOn
import pw.aru.utils.extensions.initials
import pw.aru.utils.extensions.simpleName
import java.lang.System.currentTimeMillis

class ErrorReporter {
    companion object : KLogging() {
        private val discordSnowflake by lazy { SnowflakeConfig(MiscUtil.DISCORD_EPOCH) }
    }

    private var errorId: String? = null
    private var timestamp: Long? = null
    private var command: ICommand? = null
    private var member: Member? = null
    private var throwable: Throwable? = null
    private var underlyingThrowable: Throwable? = null
    private var log: String? = null
    private var guild: Guild? = null
    private var channel: TextChannel? = null
    private var message: Message? = null
    private var extra: MutableMap<String, Any?>? = null

    fun command(command: ICommand) = apply {
        this.command = command
    }

    fun errorId(errorId: String) = apply {
        this.errorId = errorId
    }

    @JvmOverloads
    fun throwable(throwable: Throwable, underlyingThrowable: Throwable? = null) = apply {
        this.throwable = throwable
        if (underlyingThrowable != null) this.underlyingThrowable = underlyingThrowable
    }

    fun underlyingThrowable(underlyingThrowable: Throwable?) = apply {
        if (this.throwable == null && underlyingThrowable != null) {
            this.throwable = underlyingThrowable
        } else {
            if (underlyingThrowable != this.throwable)
                this.underlyingThrowable = underlyingThrowable
        }
    }

    @JvmOverloads
    fun exception(exception: Exception, underlyingException: Exception? = null) = throwable(exception, underlyingException)
    fun underlyingException(underlyingException: Exception?) = underlyingThrowable(underlyingException)

    fun message(event: GuildMessageReceivedEvent) = message(event.message)

    fun message(message: Message) = apply {
        this.message = message
    }.timestampFromMessage().member(message.member).channel(message.textChannel)

    fun channel(channel: TextChannel) = apply {
        this.channel = channel
    }.guild(channel.guild)

    fun guild(guild: Guild) = apply {
        this.guild = guild
    }

    fun member(member: Member) = apply {
        this.member = member
    }

    fun log(log: String) = apply {
        this.log = log
    }

    fun extra(key: String, value: Any?) = applyOn(extra ?: LinkedHashMap<String, Any?>().also { extra = it }) {
        this[key] = value
    }

    fun appendMdc() = appendMdc(MDC.getCopyOfContextMap())

    fun appendMdc(mdc: Map<String, String>) = extra("mdc", mdc)

    fun logTimestamp() = apply {
        this.timestamp = currentTimeMillis()
    }

    fun timestampFromMessage() = apply {
        this.timestamp = discordSnowflake.getCreationTimeMillis(message!!.idLong)
    }

    fun errorIdFromContext() = apply {
        val ex = throwable
        val m = message?.idLong
        val t = timestamp

        this.errorId = when {
            ex != null && m != null -> ex.simpleName().initials() + "#" + Snow64.fromSnowflake(m)
            ex == null && m != null -> "%" + Snow64.fromSnowflake(m)
            ex != null && t != null -> ex.simpleName().initials() + "#T:" + Snow64.fromSnowflake(t)
            ex == null && t != null -> "#T:" + Snow64.fromSnowflake(t)
            else -> throw IllegalStateException()
        }
    }

    fun report() = ErrorReportHandler(ErrorReport(command, timestamp, errorId, throwable, underlyingThrowable, log, guild, channel, message, member, extra))
}

internal data class ErrorReport internal constructor(
    val command: ICommand?,
    val timestamp: Long?,
    val errorId: String?,
    val throwable: Throwable?,
    val underlyingThrowable: Throwable?,
    val log: String?,
    val guild: Guild?,
    val channel: TextChannel?,
    val message: Message?,
    val member: Member?,
    val extra: Map<String, Any?>?
)
