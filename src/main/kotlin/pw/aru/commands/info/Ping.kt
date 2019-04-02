package pw.aru.commands.info

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Help
import pw.aru.utils.extensions.lang.invoke
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.text.PING_PONG
import java.lang.System.currentTimeMillis
import java.util.concurrent.CompletionStage

@Command("ping")
class Ping : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    private val messages = listOf(
        "I won! *Faster than Discord!*",
        "Aw, I lost! *Discord is too fast.*"
    )

    override fun CommandContext.call() {
        val start = currentTimeMillis()
        channel.triggerTypingIndicator().thenRun {
            val ping = currentTimeMillis() - start

            val gatewayPing = catnip.shardManager().run { List(shardCount(), ::latency) }
                .map(CompletionStage<Long>::invoke).average()

            send("$PING_PONG ${messages.random()}\n**Ping**: API - `${ping}ms` / Gateway - `${gatewayPing}ms`")
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("ping"), "Ping Command"),
        Description("**Plays Ping-Pong with Discord and finds out how much it takes to**.")
    )
}