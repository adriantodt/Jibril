package pw.aru.core.commands

import pw.aru.core.CommandRegistry

interface ICommandProvider {
    fun provide(r: CommandRegistry)
}
