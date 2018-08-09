package pw.aru.commands.utils

import net.dv8tion.jda.core.entities.IFakeable
import pw.aru.core.categories.Categories
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.CommandUsage.Companion.prefix
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Example
import pw.aru.core.commands.help.Help
import pw.aru.utils.twemoji_pattern

@Command("poll")
class Poll : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category: Category = Categories.UTILS

    override fun CommandContext.call() = showHelp()

    private val pattern = Regex(twemoji_pattern)

    override fun CommandContext.discreteCall(outer: String) {
        val emotes = event.message.emotes.filterNot(IFakeable::isFake).map { it.asMention to "${it.name}:${it.id}" }
        outer.split('\n')
            .mapNotNull { pattern.find(it.trimStart())?.value ?: emotes.firstOrNull { (e) -> it.trimStart().startsWith(e) }?.second }
            .distinct()
            .forEach { event.message.addReaction(it).queue() }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("poll"), "Poll Command"),
        Description("Creates a new poll. It supports most emoji and local emotes."),
        Example(
            "[$prefix${"poll"}] Should we play Fortnite or PUGB?",
            ":one: Fornite", ":two: PUGB",
            withPrefix = false
        )
    )
}

private fun <T> T?.log(s: String? = null): T? {
    println("$s${this}")
    return this
}
