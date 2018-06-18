package pw.aru.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.parser.Args

abstract class ArgsCommand : ICommand {
    override fun call(event: GuildMessageReceivedEvent, args: String) = call(event, Args(args))

    abstract fun call(event: GuildMessageReceivedEvent, args: Args)
}