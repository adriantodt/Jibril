package pw.aru.utils.commands

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.utils.extensions.embed
import pw.aru.utils.extensions.invoke
import java.util.concurrent.Future

class EmbedFirst(event: GuildMessageReceivedEvent, private val embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) {
    private var msg: Future<Message> = event.channel.sendMessage(embed(embed, init)).submit()

    infix fun then(then: EmbedBuilder.() -> Unit) = apply {
        val embed = embed(embed, then)
        msg = msg().editMessage(embed).submit()
    }
}