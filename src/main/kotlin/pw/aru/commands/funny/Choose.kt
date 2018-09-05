package pw.aru.commands.funny

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.random

@Command("choose")
class Choose : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.FUN

    override fun CommandContext.call() {
        val options = args.split(',').map(String::trim).filterNot(String::isEmpty)

        if (options.isEmpty()) return showHelp()

        send("$THINKING Hmmm... I choose `${options.random()}`!").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("choose"), "Choose Command", thumbnail = "https://assets.aru.pw/img/category/fun.png"),
        Description("Decisions are though, huh? Let me choose between the options for you."),
        Usage(
            CommandUsage("choose <option 1>, <option 2>, [other options separated by comma...]", "Choose one of the options.")
        )
    )
}