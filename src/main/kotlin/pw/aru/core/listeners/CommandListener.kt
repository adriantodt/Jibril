package pw.aru.core.listeners

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission.ADMINISTRATOR
import com.mewna.catnip.entity.util.Permission.SEND_MESSAGES
import pw.aru.core.CommandProcessor
import pw.aru.utils.AruTaskExecutor.queue
import java.util.function.Consumer

class CommandListener(private val processor: CommandProcessor) : Consumer<Message> {
    override fun accept(message: Message) {
        val guild = message.guild() ?: return

        if (message.author().bot()) return

        val self = message.catnip().selfUser() ?: return

        val selfMember = guild.members().getById(self.idAsLong())

        if (!selfMember.hasPermissions(message.channel().asGuildChannel(), SEND_MESSAGES)
            && !selfMember.hasPermissions(ADMINISTRATOR)
        ) return

        queue("Cmd:${message.author().username()}#${message.author().discriminator()}:${message.content()}") {
            processor.onCommand(message)
        }
    }
}
