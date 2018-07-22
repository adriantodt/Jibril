@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.X

object CommandExceptions {
    object ShowHelp : RuntimeException()
}

inline fun showHelp(): Unit = throw CommandExceptions.ShowHelp

fun onHelp(command: ICommand, event: GuildMessageReceivedEvent) {
    if (command is ICommand.HelpProvider) {
        command.helpHandler.onHelp(event)
        return
    }

    if (command is ICommand.HelpDialogProvider) {
        event.channel.sendMessage(command.helpHandler.onHelp(event)).queue()
        return
    }

    if (command is ICommand.HelpHandler) {
        command.onHelp(event)
        return
    }

    if (command is ICommand.HelpDialog) {
        event.channel.sendMessage(command.onHelp(event)).queue()
        return
    }

    event.channel.sendMessage("$ERROR Heh. Sorry, but the command doesn't provide any help. I can still pat you, right?").queue()
}

fun onHelp(category: Category, event: GuildMessageReceivedEvent) {
    if (category is ICommand.HelpProvider) {
        category.helpHandler.onHelp(event)
        return
    }

    if (category is ICommand.HelpDialogProvider) {
        event.channel.sendMessage(category.helpHandler.onHelp(event)).queue()
        return
    }
    if (category is ICommand.HelpHandler) {
        category.onHelp(event)
        return
    }

    if (category is ICommand.HelpDialog) {
        event.channel.sendMessage(category.onHelp(event)).queue()
        return
    }

    event.channel.sendMessage("$ERROR Heh. Sorry, but the category doesn't provide any help. I can still pat you, right?").queue()
}

const val ERROR_GUILD_PERMS = "You can **easily** fix that by re-inviting me with the following link: ``https://add.aru.pw/``"
const val ERROR_CHANNEL_PERMS = "Fix the **current channel**'s permissions and enable me the missing permissions shown above."
const val ERROR_CHANNEL_NOT_NSFW = "For this ccommand to work, set this text channel to **NSFW**."

fun requireNsfw(event: GuildMessageReceivedEvent): Boolean {
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

fun requirePerms(event: GuildMessageReceivedEvent, vararg permissions: Permission): Boolean {
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

inline fun String.withPrefix() = "${HelpFactory.prefix}$this"

inline fun String.usage(description: String): String = "`${this.withPrefix()}` - $description"

inline fun String.usage(extra: String, description: String): String = "`${this.withPrefix()}` $extra - $description"