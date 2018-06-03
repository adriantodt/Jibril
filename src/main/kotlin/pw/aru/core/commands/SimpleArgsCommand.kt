package pw.aru.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import xyz.cuteclouds.utils.StringUtils

abstract class SimpleArgsCommand(
    private val expectedArgs: Int = 0,
    private val rest: Boolean = false
) : CommandWithArgs<Array<String>>() {
    override fun args(event: GuildMessageReceivedEvent, args: String): Array<String> = StringUtils.splitArgs(args, expectedArgs, rest)
}