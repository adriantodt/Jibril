package pw.aru.bot.commands.context

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.message.Embed
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.message.MessageOptions
import org.slf4j.MDC
import pw.aru.bot.commands.ICommand
import pw.aru.bot.music.entities.MusicEventSource
import pw.aru.bot.parser.Args
import pw.aru.bot.reporting.ErrorReporter
import pw.aru.utils.extensions.aru.ERROR_CHANNEL_NOT_NSFW
import pw.aru.utils.extensions.aru.ERROR_CHANNEL_PERMS
import pw.aru.utils.extensions.aru.ERROR_GUILD_PERMS
import pw.aru.utils.extensions.lang.handlers
import pw.aru.utils.extensions.lib.embed
import pw.aru.utils.extensions.lib.message
import pw.aru.utils.text.X

private typealias AruPermission = pw.aru.core.permissions.Permission
private typealias CatnipPermission = com.mewna.catnip.entity.util.Permission

data class CommandContext(
    val message: Message,
    val args: String,
    val permissions: Set<AruPermission>
) {
    val catnip: Catnip
        get() = message.catnip()

    val author by lazy { ContextMember(message.author(), message.member()!!) }

    val channel: TextChannel
        get() = message.channel().asTextChannel()

    val guild: Guild
        get() = message.guild()!!

    val self by lazy { ContextMember(catnip.selfUser()!!, guild.selfMember()) }

    fun parseable() = Args(args)

    fun showHelp(): Unit = throw ShowHelp

    fun <T> returnHelp(): T = throw ShowHelp

    fun sendEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) =
        channel.sendMessage(embed(builder, init))

    fun sendMessage(init: MessageOptions.() -> Unit) = channel.sendMessage(message(init))

    fun send(text: String) = channel.sendMessage(text)

    fun send(embed: Embed) = channel.sendMessage(embed)

    fun send(message: Message) = channel.sendMessage(message)

    fun <T, R> T.withMDC(vararg pairs: Pair<String, String>, block: T.() -> R) {
        for ((k, v) in pairs) MDC.put(k, v)
        try {
            block()
        } finally {
            for ((k) in pairs) MDC.remove(k)
        }
    }

    fun <T> ICommand.ExceptionHandler.handlingExceptions(success: (T) -> Unit = {}): (T, Throwable?) -> Unit {
        return handlers(success, handleException())
    }

    fun asMusicSource() = MusicEventSource.Discord(author, channel)


    fun ICommand.ExceptionHandler.handleException(): (Throwable) -> Unit {
        val mdc = MDC.getCopyOfContextMap()
        return {
            try {
                handle(message, it)
            } catch (underlying: Exception) {
                ErrorReporter()
                    .command(this as ICommand)
                    .throwable(it)
                    .underlyingException(underlying)
                    .message(message)
                    .errorIdFromContext()
                    .appendMdc(mdc)
                    .report()
                    .logToFile()
                    .logAsError()
                    .sendErrorMessage()
            }
        }
    }

    fun <T> ICommand.exceptionHandling(success: (T) -> Unit = {}): (T, Throwable?) -> Unit {
        return handlers(success, exceptionHandler())
    }


    fun ICommand.exceptionHandler(): (Throwable) -> Unit {
        val mdc = MDC.getCopyOfContextMap()
        return {
            ErrorReporter()
                .command(this)
                .throwable(it)
                .message(message)
                .errorIdFromContext()
                .appendMdc(mdc)
                .report()
                .logToFile()
                .logAsError()
                .sendErrorMessage()
        }
    }

    fun requireNSFW(): Boolean {
        if (!channel.nsfw()) {
            send("$X S-Sorry, but this channel is not a **NSFW** channel!\n$ERROR_CHANNEL_NOT_NSFW")
            return false
        }

        return true
    }

    fun requirePerms(vararg permissions: CatnipPermission): Boolean {
        if (!self.hasPermissions(channel, *permissions)) {
            val guildCheck = self.hasPermissions(*permissions)
            val perms = permissions.map { it to self.hasPermissions(channel, it) }

            send(
                arrayOf(
                    "$X For this command to work, I need the following permissions:",
                    perms.joinToString("\n") { (perm, enabled) -> "${if (enabled) "✅" else "❎"} **${perm.permName()}**" },
                    "",
                    if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                    "If you need help on doing that, check my support server: ``https://support.aru.pw/``"
                ).joinToString("\n")
            )

            return false
        }

        return true
    }

    object ShowHelp : RuntimeException()
}

