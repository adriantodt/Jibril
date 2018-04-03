package jibril.utils.commands

import jibril.utils.extensions.embed
import jibril.utils.extensions.invoke
import jibril.utils.extensions.send
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.Future

class EmbedFirst(event: GuildMessageReceivedEvent, private val embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) {
    private var msg: Future<Message> = embed(embed, init).send(event).submit()

    infix fun then(then: EmbedBuilder.() -> Unit) = apply {
        val embed = embed(embed, then)
        msg = msg().editMessage(embed).submit()
    }
}