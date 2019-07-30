package pw.aru.utils

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.message.Message
import pw.aru.utils.extensions.lib.embed

class EmbedFirst(message: Message, init: EmbedBuilder.() -> Unit) {
    private val builder: EmbedBuilder = EmbedBuilder()
    private var msg = message.channel().sendMessage(embed(builder, init))

    infix fun then(then: EmbedBuilder.() -> Unit) = apply {
        val embed = embed(builder, then)
        msg = msg.flatMap { it.edit(embed) }.apply { subscribe() }
    }
}