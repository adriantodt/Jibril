package jibril.commands.eastereggs

import br.com.brjdevs.java.utils.collections.CollectionUtils.random
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.CommandPermission.BOT_DEVELOPER
import jibril.core.commands.CommandPermission.SERVER_ADMIN
import jibril.core.commands.ICommand
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.text.MessageFormat
import java.util.*

@Command("hi", "hai", "hello")
class Greeting : ICommand {
    override val category: Category? = null

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        event.channel.sendMessage(random(event.member).format(arrayOf(event.member.effectiveName))).queue()
    }

    companion object {
        private val speeches = listOf(
            "Oh, hai {0}, how's going?",
            "Hai! I'm Jibril, what's your name!",
            "H-hai!",
            "Hello everyone!"
        )

        private val speechesAdmin = listOf(
            "Oh, {0}!! How are you?",
            "Hi {0}! Hope you're having a great day!"
        )

        private val speechesOwner = listOf(
            "Oh, {0}!! How are you?",
            "Oh, haii {0}! How are you, boi?",
            "Hi {0}! Hope you're having a wonderful day!"
        )

        private fun random(member: Member): MessageFormat {
            return random(
                when {
                    BOT_DEVELOPER.test(member) -> speechesOwner
                    SERVER_ADMIN.test(member) -> speechesAdmin
                    else -> speeches
                }
            ).toMessageFormat()
        }

        private val cached = HashMap<String, MessageFormat>()

        private fun String.toMessageFormat() = cached.computeIfAbsent(replace("'", "''"), ::MessageFormat)
    }

}