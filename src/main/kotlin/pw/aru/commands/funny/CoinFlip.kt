package pw.aru.commands.funny

import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.COIN_HEADS
import pw.aru.utils.emotes.COIN_TAILS
import pw.aru.utils.extensions.random
import pw.aru.utils.extensions.stripFormatting
import pw.aru.utils.extensions.threadLocalRandom

@Command("coinflip", "flip", "coin")
class CoinFlip : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    private val sThrow = listOf(
        "Here it goes...",
        "Oh, I love doing this!"
    )

    override fun CommandContext.call() {
        event.channel.sendMessage("*${sThrow.random()}*\n${if (threadLocalRandom().nextBoolean()) heads else tails}").queue()
    }

    override fun CommandContext.discreteCall(outer: String) {
        val toSend = outer.replace('\n', ' ').stripFormatting()

        event.channel.sendMessage("**$toSend**\n${if (threadLocalRandom().nextBoolean()) heads else tails}").queue()
    }

    override val helpHandler = HelpFactory("CoinFlip Command") {
        aliases("flip", "coin")
        description("Have some fun, flip a coin.")
    }

    companion object {
        private const val heads = "$COIN_HEADS Heads!"
        private const val tails = "$COIN_TAILS Tails!"
    }
}