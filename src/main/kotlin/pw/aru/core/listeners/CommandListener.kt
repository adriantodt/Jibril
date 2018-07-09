package pw.aru.core.listeners

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.CommandProcessor
import pw.aru.core.listeners.EventListeners.submitTask
import pw.aru.utils.extensions.classOf

class CommandListener(private val processor: CommandProcessor) : OptimizedListener<GuildMessageReceivedEvent>(classOf()) {
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

        submitTask("Cmd:${event.author.name}#${event.author.discriminator}:${event.message.contentRaw}") {
            processor.onCommand(event)
        }
    }
}
