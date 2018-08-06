package pw.aru.commands.info

import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Help
import pw.aru.utils.emotes.PING_PONG
import pw.aru.utils.extensions.random
import java.lang.System.currentTimeMillis

@Command("ping")
class Ping : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    private val messages = listOf(
        "I won! *Faster than Discord!*",
        "Aw, I lost! *Discord is too fast.*"
    )

    override fun CommandContext.call() {
        val start = currentTimeMillis()
        event.channel.sendTyping().queue {
            val ping = currentTimeMillis() - start
            send(
                "$PING_PONG ${messages.random()}\n**Ping**: API - `${ping}ms` | Websocket - `${event.jda.ping}ms`"
            ).queue()
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("ping"), "Ping Command"),
        Description("**Plays Ping-Pong with Discord and finds out how much it takes to**.")
    )
}