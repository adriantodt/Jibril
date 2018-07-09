package pw.aru.commands.funny

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.THINKING

@Command("ratewaifu", "rw")
class RateWaifu : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (event.message.mentionedUsers.size > 1) {
            event.channel.sendMessage("$ERROR Too many waifus to rate! Don't mention more than one user at a time.").queue()
            return
        }

        val toRate = if (args.isEmpty()) event.author.asMention
        else args
            .replace("<@!", "<@")
            .splitToSequence(' ', '\r', '\n')
            .filter(String::isNotBlank)
            .joinToString(" ")
            .trim()

        val rating = when (toRate.toLowerCase()) {
            "mantaro", "mantaro patreon", "mantaro premium", "<@213466096718708737>", "<@302810118335102976>" -> 95
            "aru dev", "arudev", "jibrildev", "jibril dev", "<@406945939148898304>" -> 99
            "aru", "jibril", "<@406082711271374848>" -> 100
            else -> (toRate.map(Char::toLong).map { it * 2 }.sum() + 50) % 101
        }

        event.channel.sendMessage("$THINKING Hmmm... I think **$toRate** is worth a $rating/100, don't you?").queue()
    }

    override val helpHandler = HelpFactory("RateWaifu Command") {
        aliases("rw")
        description("Rates your waifu from zero to 100")

        usage("ratewaifu", "Rates you.")
        usage("ratewaifu <name/mention>", "Rates the name/user.")

        seeAlso("choose")
    }
}