package pw.aru.utils.commands

import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.utils.extensions.invoke
import pw.aru.utils.extensions.message
import java.util.concurrent.Future

class MessageFirst(event: GuildMessageReceivedEvent, private val message: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit) {
    private var msg: Future<Message> = event.channel.sendMessage(message(message, init)).submit()

    infix fun then(then: MessageBuilder.() -> Unit) = apply {
        val embed = message(message, then)
        msg = msg().editMessage(embed).submit()
    }
}