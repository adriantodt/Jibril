package jibril.commands.info

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.PING_PONG
import jibril.utils.extensions.random
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.lang.System.currentTimeMillis

@Command("ping")
class Ping : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    private val messages = listOf(
        "I won! *Faster than Discord!*",
        "Aw, I lost! *Discord is too fast.*"
    )

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        val start = currentTimeMillis()
        event.channel.sendTyping().queue {
            val ping = currentTimeMillis() - start
            event.channel.sendMessage(
                "$PING_PONG ${messages.random()}\n**Ping**: API - `${ping}ms` | Websocket - `${event.jda.ping}ms`"
            ).queue()
        }
    }

    override val helpHandler = HelpFactory("Ping Command") {
        description("**Plays Ping-Pong with Discord and finds out how much it takes to**.")
    }
}