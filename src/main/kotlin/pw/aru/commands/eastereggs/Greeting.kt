package pw.aru.commands.eastereggs

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission.BOT_DEVELOPER
import pw.aru.core.commands.CommandPermission.SERVER_ADMIN
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.emotes.EYES
import pw.aru.utils.extensions.random
import java.text.MessageFormat
import java.util.*

@Command("hi", "hai", "hello")
class Greeting : ICommand, ICommand.HelpHandler {
    override fun onHelp(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(EYES).queue()
    }

    override val category = null

    override fun CommandContext.call() {
        send(random(event.member).format(arrayOf(event.member.effectiveName))).queue()
    }

    companion object {
        private val speeches = listOf(
            "Oh, hai {0}, how's going?",
            "Hai! I'm Aru, what's your name!",
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
            return when {
                BOT_DEVELOPER.test(member) -> speechesOwner
                SERVER_ADMIN.test(member) -> speechesAdmin
                else -> speeches
            }.random().toMessageFormat()
        }

        private val cached = HashMap<String, MessageFormat>()

        private fun String.toMessageFormat() = cached.computeIfAbsent(replace("'", "''"), ::MessageFormat)
    }

}