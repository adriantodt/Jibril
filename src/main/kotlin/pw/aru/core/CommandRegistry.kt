package pw.aru.core

import mu.KLogging
import pw.aru.core.commands.ICommand
import pw.aru.utils.extensions.lang.classOf

class CommandRegistry {
    companion object : KLogging() {
        private val helpInterfaces = listOf(
            classOf<ICommand.HelpDialogProvider>(),
            classOf<ICommand.HelpDialog>()
        )
    }

    val commands: MutableMap<String, ICommand> = LinkedHashMap()
    val lookup: MutableMap<ICommand, MutableList<String>> = LinkedHashMap()

    operator fun get(key: String) = commands[key]

    operator fun set(vararg names: String, command: ICommand) {
        register(names.toList(), command)
    }

    fun register(names: List<String>, command: ICommand) {
        sanityChecks(command, names)

        val keys = names.asSequence()
            .map(String::toLowerCase)
            .distinct()
            .onEach { commands[it] = command }

        lookup.getOrPut(command, ::ArrayList).addAll(keys)
    }

    private fun sanityChecks(command: ICommand, names: List<String>) {
        if (names.isEmpty()) {
            logger.error { "Command \"${command.javaClass.name}\" doesn't has any defined names." }

            throw IllegalStateException("empty array")
        }

        val implemented = helpInterfaces.filter { it.isInstance(command) }

        if (implemented.isEmpty()) {
            logger.warn { "Command \"${command.javaClass.name}\" doesn't implement a help interface." }
        } else if (implemented.size > 1) {
            logger.warn { "Command \"${command.javaClass.name}\" implements multiple interfaces: ${implemented.joinToString { it.name }}. Implementation ${implemented.first().name} will be used" }
        }
    }
}
