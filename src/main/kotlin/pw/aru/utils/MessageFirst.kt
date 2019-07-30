package pw.aru.utils

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.message.MessageOptions

class MessageFirst(message: Message, init: MessageOptions.() -> Unit) {
    private val builder: MessageOptions = MessageOptions()
    private var msg = message.channel().sendMessage(builder.also(init).buildMessage())

    infix fun then(then: MessageOptions.() -> Unit) = apply {
        val message = builder.also(then).buildMessage()
        msg = msg.flatMap { it.edit(message) }.apply { subscribe() }
    }
}