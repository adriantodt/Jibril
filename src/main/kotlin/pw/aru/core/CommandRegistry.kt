package pw.aru.core

import mu.KLogging
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.placeholder.PlaceholderCommand
import pw.aru.core.commands.placeholder.ReRoutingPlaceholderCommand
import pw.aru.utils.extensions.classOf

object CommandRegistry : KLogging() {
    val commands: MutableMap<String, ICommand> = LinkedHashMap()
    val lookup: MutableMap<ICommand, Array<String>> = LinkedHashMap()

    private val helpInterfaces = listOf(
        classOf<ICommand.HelpDialogProvider>(),
        classOf<ICommand.HelpProvider>(),
        classOf<ICommand.HelpDialog>(),
        classOf<ICommand.HelpHandler>()
    )

    private fun sanityChecks(command: ICommand, names: Array<out String>) {
        check(names.isNotEmpty()) {
            logger.error { "Command \"${command.javaClass.name}\" doesn't has any defined names." }

            "empty array"
        }

        val implemented = helpInterfaces.filter { it.isInstance(command) }

        if (implemented.isEmpty()) {
            logger.warn { "Command \"${command.javaClass.name}\" doesn't implement a help interface." }
        } else if (implemented.size > 1) {
            logger.warn { "Command \"${command.javaClass.name}\" implements multiple interfaces: ${implemented.joinToString { it.name }}. Implementation ${implemented.first().name} will be used" }
        }
    }

    fun registerPlaceholder(names: Array<out String>, logCalls: Boolean) {
        val command = if (logCalls) ReRoutingPlaceholderCommand() else PlaceholderCommand
        val keys = names.map(String::toLowerCase).distinct().toTypedArray()

        for (k in keys) commands[k] = command
        lookup[command] = keys
    }

    fun register(names: Array<out String>, command: ICommand) {
        sanityChecks(command, names)

        val keys = names.map(String::toLowerCase).distinct().toTypedArray()
        for (k in keys) commands[k] = command
        lookup[command] = keys
    }

    fun registerOverride(names: Array<out String>, command: ICommand): List<Pair<GuildMessageReceivedEvent, String>> {
        sanityChecks(command, names)
        val keys = names.map(String::toLowerCase).distinct().toTypedArray()

        val placeholder = commands[keys[0]]

        val logs: List<Pair<GuildMessageReceivedEvent, String>> = when (placeholder) {
            is ReRoutingPlaceholderCommand -> placeholder.queue
            is PlaceholderCommand -> emptyList()
            else -> throw IllegalStateException("Command is not a placeholder")
        }

        for (k in keys) commands[k] = command
        lookup[command] = keys

        return logs
    }
}
