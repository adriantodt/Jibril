@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions.aru

import com.mewna.catnip.entity.message.Message
import pw.aru.bot.commands.ICommand
import pw.aru.utils.text.ERROR

fun onHelp(command: ICommand, message: Message) {
    if (command is ICommand.HelpDialogProvider) {
        message.channel().sendMessage(command.helpHandler.onHelp(message))
        return
    }

    if (command is ICommand.HelpDialog) {
        message.channel().sendMessage(command.onHelp(message))
        return
    }

    message.channel()
        .sendMessage("$ERROR Heh. Sorry, but the command doesn't provide any help. I can still pat you, right?")
}

const val ERROR_GUILD_PERMS =
    "You can **easily** fix that by re-inviting me with the following link: `https://add.aru.pw/`"
const val ERROR_CHANNEL_PERMS =
    "Fix the **current channel**'s permissions and enable me the missing permissions shown above."
const val ERROR_CHANNEL_NOT_NSFW = "For this command to work, set this text channel to **NSFW**."