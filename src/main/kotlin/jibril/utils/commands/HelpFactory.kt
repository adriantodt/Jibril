package jibril.utils.commands

import jibril.core.commands.CommandPermission
import jibril.core.commands.ICommand
import jibril.data.config.ConfigManager
import jibril.utils.JibrilColors
import jibril.utils.extensions.*
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color
import java.util.*

class HelpFactory(
    val name: String,
    private val permission: CommandPermission? = null,
    private val init: HelpFactory.() -> Unit = {}
) : ICommand.HelpDialog {

    private val aliases = LinkedList<String>()
    private val usages = LinkedList<String>()
    private val examples = LinkedList<String>()
    private val seeAlso = LinkedList<String>()
    private var description: String? = null
    private var tutorial: String? = null
    private var color: Color? = null

    private var category = false

    private var isInit = false

    override fun onHelp(event: GuildMessageReceivedEvent): MessageEmbed {
        synchronized(this) {
            if (!isInit) {
                isInit = true
                init(this)
            }
        }

        return embed {
            helpEmbed(event, name, permission, color = color ?: JibrilColors.primary)

            if (aliases.isNotEmpty()) {
                field("Aliases:", aliases.joinToString("` `", "`", "`"))
            }

            if (description != null) {
                field("Description:", description!!, false)
            }

            if (tutorial != null) {
                field("Tutorial:", tutorial!!, false)
            }

            if (usages.isNotEmpty()) {
                field(if (category) "Commands:" else "Usages:", usages.joinToString("\n"), false)
            }

            if (examples.isNotEmpty()) {
                field(
                    "Examples:",
                    examples.joinToString(prefix = "```\n", separator = "\n", postfix = "\n```") { it.withPrefix() }
                )
            }

            if (seeAlso.isNotEmpty()) {
                field(if (category) "Other Commands:" else "See Also:", seeAlso.joinToString("\n"), false)
            }
        }
    }

    fun categoryMode() {
        category = true
    }

    fun aliases(vararg values: String) {
        aliases.addAll(values)
    }

    fun color(color: Color) {
        this.color = color
    }

    fun description(vararg value: String) {
        description = value.joinToString("\n")
    }

    fun tutorial(vararg value: String) {
        tutorial = value.joinToString("\n")
    }

    fun usageSeparator() {
        usages.add("")
    }

    fun usageNote(note: String) {
        usages.add(note)
    }

    fun usage(command: String, description: String) {
        usages.add(command.usage(description))
    }

    fun usage(command: String, extra: String, description: String) {
        usages.add(command.usage(extra, description))
    }

    fun alsoSee(command: String, description: String) {
        seeAlso.add(command.usage(description))
    }

    fun alsoSee(command: String, extra: String, description: String) {
        seeAlso.add(command.usage(extra, description))
    }

    fun seeAlso(vararg commands: String) {
        seeAlso.add(commands.joinToString("` `", "`", "`"))
    }

    fun examples(vararg commands: String) {
        examples.addAll(commands)
    }

    companion object {
        val prefix = ConfigManager.config.prefixes.first()
    }
}