package pw.aru.core.commands.context

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import org.slf4j.MDC
import pw.aru.core.commands.ICommand
import pw.aru.core.parser.Args
import pw.aru.core.reporting.ErrorReporter
import pw.aru.utils.emotes.X
import pw.aru.utils.extensions.*

data class CommandContext(
    val event: GuildMessageReceivedEvent,
    val args: String
) {
    val message: Message
        get() = event.message

    val author: Member
        get() = event.member

    val channel: TextChannel
        get() = event.channel

    val guild: Guild
        get() = event.guild

    val selfMember: Member
        get() = event.guild.selfMember

    val selfUser: SelfUser
        get() = event.jda.selfUser

    fun parseable() = Args(args)

    fun showHelp(): Unit = throw ShowHelp

    //Self-explanatory
    fun <T> returnHelp(): T = throw ShowHelp

    fun sendEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): MessageAction = event.channel.sendMessage(embed(builder, init))

    fun sendMessage(builder: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit): MessageAction = event.channel.sendMessage(message(builder, init))

    fun send(text: CharSequence): MessageAction = event.channel.sendMessage(text)

    fun send(embed: MessageEmbed): MessageAction = event.channel.sendMessage(embed)

    fun send(message: Message): MessageAction = event.channel.sendMessage(message)

    fun <T, R> T.withMDC(vararg pairs: Pair<String, String>, block: T.() -> R) {
        for ((k, v) in pairs) MDC.put(k, v)
        try {
            block()
        } finally {
            for ((k, v) in pairs) MDC.remove(k)
        }
    }

    fun ICommand.ExceptionHandler.handleException(): (Throwable) -> Unit {
        val mdc = MDC.getCopyOfContextMap()

        return {
            try {
                handle(event, it)
            } catch (underlying: Exception) {
                ErrorReporter()
                    .command(this as ICommand)
                    .throwable(it)
                    .underlyingException(underlying)
                    .message(event)
                    .errorIdFromContext()
                    .appendMdc(mdc)
                    .report()
                    .logToFile()
                    .logAsError()
                    .sendErrorMessage()
            }
        }
    }

    fun ICommand.exceptionHandler(): (Throwable) -> Unit {
        val mdc = MDC.getCopyOfContextMap()
        return {
            ErrorReporter()
                .command(this)
                .throwable(it)
                .message(event)
                .errorIdFromContext()
                .appendMdc(mdc)
                .report()
                .logToFile()
                .logAsError()
                .sendErrorMessage()
        }
    }

    fun requireNSFW(): Boolean {
        if (!channel.isNSFW) {
            send("$X S-Sorry, but this channel is not a **NSFW** channel!\n$ERROR_CHANNEL_NOT_NSFW").queue()
            return false
        }

        return true
    }

    fun requirePerms(vararg permissions: Permission): Boolean {
        if (!selfMember.hasPermission(channel, *permissions)) {
            val guildCheck = selfMember.hasPermission(*permissions)
            val perms = permissions.map { it to selfMember.hasPermission(channel, it) }

            send(
                arrayOf(
                    "$X For this command to work, I need the following permissions:",
                    perms.joinToString("\n") { (perm, enabled) -> "${if (enabled) "✅" else "❎"} **${perm.getName()}**" },
                    "",
                    if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                    "If you need help on doing that, check my support server: ``https://support.aru.pw/``"
                ).joinToString("\n")
            ).queue()

            return false
        }

        return true
    }

    object ShowHelp : RuntimeException()
}

