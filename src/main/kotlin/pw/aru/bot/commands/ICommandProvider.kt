package pw.aru.bot.commands

import pw.aru.bot.CommandRegistry

interface ICommandProvider {
    fun provide(r: CommandRegistry)
}
