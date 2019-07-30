package pw.aru.commands.info

import io.reactivex.Single
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.Description
import pw.aru.bot.commands.help.Help
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.text.PING_PONG
import java.lang.System.currentTimeMillis

@Command("ping")
class Ping : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    private val messages = listOf(
        "I won! *Faster than Discord!*",
        "Aw, I lost! *Discord is too fast.*"
    )

    override fun CommandContext.call() {
        val start = currentTimeMillis()
        channel.triggerTypingIndicator().subscribe {
            val ping = currentTimeMillis() - start

            val gatewayPing = catnip.shardManager().run { List(shardCount(), ::latency) }
                .map(Single<Long>::blockingGet).average()

            send("$PING_PONG ${messages.random()}\n**Ping**: API - `${ping}ms` / Gateway - `${gatewayPing}ms`")
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("ping"), "Ping Command"),
        Description("**Plays Ping-Pong with Discord and finds out how much it takes to**.")
    )
}