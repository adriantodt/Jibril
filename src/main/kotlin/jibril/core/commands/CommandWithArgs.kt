package jibril.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

//Common implementations
abstract class CommandWithArgs<T> : ICommand {
    override fun call(event: GuildMessageReceivedEvent, args: String) {
        call(event, args(event, args))
    }

    protected abstract fun call(event: GuildMessageReceivedEvent, args: T)

    protected abstract fun args(event: GuildMessageReceivedEvent, args: String): T
}