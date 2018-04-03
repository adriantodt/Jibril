package jibril.core

import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.extensions.classOf
import mu.KLogging

object CommandRegistry : KLogging() {
    val commands: MutableMap<String, ICommand> = LinkedHashMap()
    val lookup: MutableMap<ICommand, Array<out String>> = LinkedHashMap()

    private val helpInterfaces = listOf(
        classOf<ICommand.HelpDialogProvider>(),
        classOf<ICommand.HelpProvider>(),
        classOf<ICommand.HelpDialog>(),
        classOf<ICommand.HelpHandler>()
    )

    private fun sanityChecks(command: ICommand) {
        val implemented = helpInterfaces.filter { it.isInstance(command) }

        if (implemented.isEmpty()) {
            logger.warn { "Command \"${command.javaClass.name}\" doesn't implement a help interface." }
        }

        if (implemented.size > 1) {
            logger.warn { "Command \"${command.javaClass.name}\" implements multiple interfaces: ${implemented.joinToString { it.name }}. Implementation ${implemented.first().name} will be used" }
        }
    }

    fun register(meta: Command, command: ICommand) {
        sanityChecks(command)
        val names = meta.value
            .map { it.toLowerCase() }
            .toTypedArray()

        if (names.isEmpty()) throw IllegalStateException("Empty annotation $meta")

        for (k in names) commands[k] = command
        lookup[command] = names
    }
}
