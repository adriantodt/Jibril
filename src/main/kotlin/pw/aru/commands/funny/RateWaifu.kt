package pw.aru.commands.funny

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.THINKING

@Command("ratewaifu", "rw")
class RateWaifu : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.FUN

    override fun CommandContext.call() {
        if (message.mentionedUsers().size > 1) {
            send("$ERROR Too many waifus to rate! Don't mention more than one user at a time.")
            return
        }

        val toRate = if (args.isEmpty()) author.asMention()
        else args.replace("<@!", "<@")
            .splitToSequence(' ', '\r', '\n')
            .filter(String::isNotBlank)
            .joinToString(" ")
            .trim()

        val rating = when (toRate.toLowerCase()) {
            "mantaro", "mantaro patreon", "mantaro premium", "<@213466096718708737>", "<@302810118335102976>" -> 95
            "aru dev", "arudev", "jibrildev", "jibril dev", "<@406945939148898304>" -> 99
            "aru", "jibril", "<@406082711271374848>" -> 100
            else -> (toRate.asSequence().map(Char::toLong).map { it * 2 }.sum() + 50) % 101
        }

        send("$THINKING Hmmm... I think **$toRate** is worth a $rating/100, don't you?")
    }

    override val helpHandler = Help(
        CommandDescription(listOf("ratewaifu", "rw"), "RateWaifu Command", thumbnail = "https://assets.aru.pw/img/category/fun.png"),
        Description("Rates your waifu from 0 to 100"),
        Usage(
            CommandUsage("ratewaifu", "Rates you."),
            CommandUsage("ratewaifu <name/mention>", "Rates the name/user.")
        ),
        SeeAlso["choose"]
    )
}