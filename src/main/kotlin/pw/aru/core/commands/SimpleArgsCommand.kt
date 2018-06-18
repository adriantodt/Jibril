package pw.aru.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import xyz.cuteclouds.utils.StringUtils.splitArgs

abstract class SimpleArgsCommand(
    private val expectedArgs: Int = 0,
    private val rest: Boolean = false
) : ICommand {
    override fun call(event: GuildMessageReceivedEvent, args: String) = call(event, splitArgs(args, expectedArgs, rest))

    abstract fun call(event: GuildMessageReceivedEvent, args: Array<String>)
}