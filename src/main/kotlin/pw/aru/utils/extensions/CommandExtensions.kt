@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.ICommand
import pw.aru.utils.emotes.ERROR

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

const val ERROR_GUILD_PERMS = "You can **easily** fix that by re-inviting me with the following link: `https://add.aru.pw/`"
const val ERROR_CHANNEL_PERMS = "Fix the **current channel**'s permissions and enable me the missing permissions shown above."
const val ERROR_CHANNEL_NOT_NSFW = "For this command to work, set this text channel to **NSFW**."