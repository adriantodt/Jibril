package pw.aru.core.listeners

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener
import pw.aru.core.CommandProcessor
import pw.aru.core.listeners.EventListeners.submitTask

class CommandListener(private val processor: CommandProcessor) : EventListener {

    override fun onEvent(event: Event) {
        if (event is GuildMessageReceivedEvent) onMessage(event)
    }

    private fun onMessage(event: GuildMessageReceivedEvent) {
        // @formatter:off
		if (
            event.author.isBot
                ||
            !event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_WRITE)
                &&
            !event.guild.selfMember.hasPermission(Permission.ADMINISTRATOR)
        ) return
        // @formatter:on

        submitTask("Cmd:${event.author.name}#${event.author.discriminator}:${event.message.contentRaw}") {
            processor.onCommand(event)
        }
    }
}
