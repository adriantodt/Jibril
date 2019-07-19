package pw.aru.commands.funny

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.Description
import pw.aru.bot.commands.help.Help
import pw.aru.utils.extensions.discordapp.stripFormatting
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.extensions.lang.randomOf
import pw.aru.utils.text.COIN_HEADS
import pw.aru.utils.text.COIN_TAILS

@Command("coinflip", "flip", "coin")
class CoinFlip : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category = Category.FUN

    private val sThrow = listOf(
        "Here it goes...",
        "Oh, I love doing this!"
    )

    override fun CommandContext.call() {
        send("*${sThrow.random()}*\n${randomOf(heads, tails)}")
    }

    override fun CommandContext.discreteCall(outer: String) {
        val toSend = outer.replace('\n', ' ').stripFormatting()

        send("**$toSend**\n${randomOf(heads, tails)}")
    }

    override val helpHandler = Help(
        CommandDescription(listOf("coinflip", "flip", "coin"), "CoinFlip Command", thumbnail = "https://assets.aru.pw/img/category/fun.png"),
        Description("Have some fun, flip a coin.")
    )

    companion object {
        private const val heads = "$COIN_HEADS Heads!"
        private const val tails = "$COIN_TAILS Tails!"
    }
}