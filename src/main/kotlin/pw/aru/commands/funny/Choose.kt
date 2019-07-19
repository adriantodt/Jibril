package pw.aru.commands.funny

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.text.THINKING

@Command("choose")
class Choose : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.FUN

    override fun CommandContext.call() {
        val options = args.splitToSequence(',')
            .map(String::trim)
            .filterNot(String::isEmpty)
            .toList()

        if (options.isEmpty()) return showHelp()

        send("$THINKING Hmmm... I choose `${options.random()}`!")
    }

    override val helpHandler = Help(
        CommandDescription(listOf("choose"), "Choose Command", thumbnail = "https://assets.aru.pw/img/category/fun.png"),
        Description("Decisions are though, huh? Let me choose between the options for you."),
        Usage(
            CommandUsage("choose <option 1>, <option 2>, [other options separated by comma...]", "Choose one of the options.")
        )
    )
}