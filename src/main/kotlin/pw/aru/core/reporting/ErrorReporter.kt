package pw.aru.core.reporting

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.util.Utils.DISCORD_EPOCH
import mu.KLogging
import org.slf4j.MDC
import pw.aru.core.commands.ICommand
import pw.aru.libs.snowflake.SnowflakeConfig
import pw.aru.libs.snowflake.snow64.Snow64
import pw.aru.utils.extensions.lang.applyOn
import pw.aru.utils.extensions.lang.initials
import pw.aru.utils.extensions.lang.simpleName

class ErrorReporter {
    companion object : KLogging() {
        private val discordSnowflake by lazy { SnowflakeConfig(DISCORD_EPOCH) }
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
    fun exception(exception: Exception, underlyingException: Exception? = null) =
        throwable(exception, underlyingException)

    fun underlyingException(underlyingException: Exception?) = underlyingThrowable(underlyingException)

    fun message(message: Message) = apply {
        this.message = message
    }.timestampFromMessage().member(message.member()!!).channel(message.channel().asTextChannel())

    fun channel(channel: TextChannel) = apply {
        this.channel = channel
    }.guild(channel.guild())

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
        this.timestamp = System.currentTimeMillis()
    }

    fun timestampFromMessage() = apply {
        this.timestamp = discordSnowflake.getCreationTimeMillis(message!!.idAsLong())
    }

    fun errorIdFromContext() = apply {
        val ex = throwable
        val m = message?.idAsLong()
        val t = timestamp

        errorId(
            when {
                ex != null && m != null -> ex.simpleName().initials() + "#" + Snow64.fromSnowflake(m)
                ex == null && m != null -> "%" + Snow64.fromSnowflake(m)
                ex != null && t != null -> ex.simpleName().initials() + "#T:" + Snow64.fromSnowflake(t)
                ex == null && t != null -> "#T:" + Snow64.fromSnowflake(t)
                else -> throw IllegalStateException()
            }
        )
    }

    fun report() = ErrorReportHandler(
        ErrorReport(
            command,
            timestamp,
            errorId,
            throwable,
            underlyingThrowable,
            log,
            guild,
            channel,
            message,
            member,
            extra
        )
    )
}