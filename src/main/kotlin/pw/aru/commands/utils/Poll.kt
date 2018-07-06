package pw.aru.commands.utils

import net.dv8tion.jda.core.entities.IFakeable
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.showHelp
import pw.aru.utils.extensions.withPrefix
import pw.aru.utils.twemoji_pattern

@Command("poll")
class Poll : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category: Category = Categories.UTILS

    override fun call(event: GuildMessageReceivedEvent, args: String) = showHelp()

    private val pattern = Regex(twemoji_pattern)

    override fun discreteCall(event: GuildMessageReceivedEvent, args: String, outer: String) {
        val emotes = event.message.emotes.filterNot(IFakeable::isFake).map { it.asMention to "${it.name}:${it.id}" }
        outer.split('\n')
            .mapNotNull { pattern.find(it.trimStart())?.value ?: emotes.firstOrNull { (e) -> it.trimStart().startsWith(e) }?.second }
            .distinct()
            .forEach { event.message.addReaction(it).queue() }
    }

    override val helpHandler = HelpFactory("Poll Command") {
        description("Creates a new poll. It supports most emoji and local emotes.")

        examples(
            "[${"poll".withPrefix()}] Should we play Fortnite or PUGB?",
            ":one: Fornite",
            ":two: PUGB",

            withPrefix = false
        )
    }
}

private fun <T> T?.log(s: String? = null): T? {
    println("$s${this}")
    return this
}
