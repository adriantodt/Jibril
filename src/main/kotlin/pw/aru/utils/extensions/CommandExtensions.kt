@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR

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

inline fun String.withPrefix() = "${HelpFactory.prefix}$this"

inline fun String.usage(description: String): String = "`${this.withPrefix()}` - $description"

inline fun String.usage(extra: String, description: String): String = "`${this.withPrefix()}` $extra - $description"