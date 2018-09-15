package pw.aru.commands.utils

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.IFakeable
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.twemoji_pattern

@Command("poll")
class Poll : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category: Category = Category.UTILS

    override fun CommandContext.call() = showHelp()

    private val pattern = Regex(twemoji_pattern)

    override fun CommandContext.discreteCall(outer: String) {
        if (!requirePerms(Permission.MESSAGE_HISTORY)) return

        val emotes = event.message.emotes.asSequence()
            .filterNot(IFakeable::isFake)
            .map { it.asMention to "${it.name}:${it.id}" }
            .toList()
        outer.splitToSequence('\n')
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
        ),
        Note("Besides the standard permissions, this command requires the **${Permission.MESSAGE_HISTORY.getName()}** permission.")
    )
}