package pw.aru.core.commands.context

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import pw.aru.core.parser.Args
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

    fun parseable() = Args(args)

    fun showHelp(): Unit = throw CommandExceptions.ShowHelp

    fun sendEmbed(builder: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): MessageAction = event.channel.sendMessage(embed(builder, init))

    fun sendMessage(builder: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit): MessageAction = event.channel.sendMessage(message(builder, init))

    fun send(text: CharSequence): MessageAction = event.channel.sendMessage(text)

    fun send(embed: MessageEmbed): MessageAction = event.channel.sendMessage(embed)

    fun requireNSFW(): Boolean {
        if (!event.channel.isNSFW) {
            event.channel.sendMessage(
                arrayOf(
                    "$X S-Sorry, but this channel is not a **NSFW** channel!",
                    ERROR_CHANNEL_NOT_NSFW
                ).joinToString("\n")
            ).queue()
            return false
        }

        return true
    }

    fun requirePerms(vararg permissions: Permission): Boolean {
        val self = event.guild.selfMember
        val channel = event.channel
        if (!self.hasPermission(channel, *permissions)) {
            val guildCheck = self.hasPermission(*permissions)
            val perms = permissions.map { it to self.hasPermission(channel, it) }

            event.channel.sendMessage(
                arrayOf(
                    "$X For this command to work, I need the following permissions:",
                    perms.joinToString("\n") { (perm, enabled) -> "${if (enabled) "✅" else "❎"} **${perm.name}**" },
                    "",
                    if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                    "If you need help on doing that, check my support server: ``https://support.aru.pw/``"
                ).joinToString("\n")
            ).queue()

            return false
        }

        return true
    }
}

