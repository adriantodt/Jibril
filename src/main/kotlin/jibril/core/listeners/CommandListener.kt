package jibril.core.listeners

import jibril.core.CommandProcessor
import jibril.core.listeners.EventListeners.submit
import jibril.utils.extensions.classOf
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

object CommandListener : OptimizedListener<GuildMessageReceivedEvent>(classOf()) {
    override fun event(event: GuildMessageReceivedEvent) {
        // @formatter:off
		if (
            event.author.isBot
                ||
            !event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_WRITE)
                &&
            !event.guild.selfMember.hasPermission(Permission.ADMINISTRATOR)
        ) return
        // @formatter:on

        submit("Cmd:${event.author.name}#${event.author.discriminator}:${event.message.contentRaw}") {
            CommandProcessor.onCommand(event)
        }
    }
}
