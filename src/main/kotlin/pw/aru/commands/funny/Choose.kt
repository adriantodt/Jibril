package pw.aru.commands.funny

import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.random

@Command("choose")
class Choose : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    override fun CommandContext.call() {
        val options = args.split(',').map(String::trim).filterNot(String::isEmpty)

        if (options.isEmpty()) return showHelp()

        send("$THINKING Hmmm... I choose `${options.random()}`!").queue()
    }

    override val helpHandler = HelpFactory("Choose Command") {
        description("Decisions are though, huh?")

        usage("choose <option 1>, <option 2>, [other options separated by comma...]", "Choose one of the options.")
    }
}