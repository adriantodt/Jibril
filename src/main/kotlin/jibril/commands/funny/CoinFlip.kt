package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.DiscordUtils
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.COIN_HEADS
import jibril.utils.emotes.COIN_TAILS
import jibril.utils.extensions.random
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

@Command("coinflip", "flip", "coin")
class CoinFlip : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {

    override val category = Categories.FUN

    private val random: Random = Random()

    private val heads = "$COIN_HEADS Heads!"
    private val tails = "$COIN_TAILS Tails!"

    private val sThrow = listOf(
        "Here it goes...",
        "Oh, I love doing this!"
    )

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        event.channel.sendMessage("*${sThrow.random()}*\n${if (random.nextBoolean()) heads else tails}").queue()
    }

    override fun discreteCall(event: GuildMessageReceivedEvent, args: String, outer: String) {
        val toSend = DiscordUtils.stripFormatting(outer.replace('\n', ' '))

        event.channel.sendMessage("**$toSend**\n${if (random.nextBoolean()) heads else tails}").queue()
    }

    override val helpHandler = HelpFactory("CoinFlip Command") {
        aliases("flip", "coin")
        description("Have some fun, flip a coin.")
    }
}