package pw.aru.commands.utils

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.util.Permission
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.text.discord_emote_pattern
import pw.aru.utils.text.twemoji_pattern

@Command("poll")
class Poll : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category: Category = Category.UTILS

    override fun CommandContext.call() = showHelp()

    private val twemoji = Regex(twemoji_pattern)
    private val discordemoji = Regex(discord_emote_pattern)

    override fun CommandContext.discreteCall(outer: String) {
        if (!requirePerms(Permission.READ_MESSAGE_HISTORY)) return

        outer.splitToSequence('\n')
            .mapNotNull {
                tryParseDiscordEmote(it.trimStart(), catnip) ?: tryParseTwemoji(it.trimStart())
            }
            .distinct()
            .forEach { message.react(it) }
    }

    private fun tryParseDiscordEmote(s: String, catnip: Catnip): String? {
        val result = discordemoji.find(s) ?: return null
        if (result.range.start != 0) return null
        val discordId = result.groups[2]?.value ?: return null

        return try {
            catnip.cache().emojis().getById(discordId).forReaction()
        } catch (e: Exception) {
            null
        }
    }

    private fun tryParseTwemoji(s: String): String? {
        val result = twemoji.find(s) ?: return null
        if (result.range.start != 0) return null
        return result.value
    }

    override val helpHandler = Help(
        CommandDescription(listOf("poll"), "Poll Command"),
        Description("Creates a new poll. It supports most emoji and local emotes."),
        Example(
            "[$prefix${"poll"}] Should we play Fortnite or PUGB?",
            ":one: Fornite", ":two: PUGB",
            withPrefix = false
        ),
        Note("Besides the standard permissions, this command requires the **${Permission.READ_MESSAGE_HISTORY.permName()}** permission.")
    )
}