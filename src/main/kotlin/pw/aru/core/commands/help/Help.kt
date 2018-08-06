package pw.aru.core.commands.help

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.ICommand
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.embed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.thumbnail

class Help(
    description: CommandDescription,
    vararg val nodes: HelpNode
) : ICommand.HelpDialog {
    val names = description.names
    val description = description.description
    val color = description.color ?: AruColors.primary
    val permission = description.permission
    val thumbnail = description.thumbnail

    override fun onHelp(event: GuildMessageReceivedEvent) = embed {
        baseEmbed(event, name = description, color = color)
        thumbnail(thumbnail)

        if (permission != null) {
            field("Permission Required:", permission.toString(), false)
        }

        if (names.size > 1) {
            field("Aliases:", names.drop(1).joinToString("` `", "`", "`"))
        }

        for (node in nodes) when (node) {
            is Description -> field("Description:", node.value)
            is Usage -> field("Usage:", node.nodes.joinToString("\n"))
            is Example -> field("Example:", node.values.joinToString(prefix = "```\n", separator = "\n", postfix = "\n```"))
            is Note -> field("Note:", node.value)
            is SeeAlso -> field("See Also:", node.value)

            is Field -> field(node.name, node.value)
        }
    }
}
