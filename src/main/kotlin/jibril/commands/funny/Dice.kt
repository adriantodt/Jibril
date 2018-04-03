package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.features.LuckyUser
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.GAME_DIE
import jibril.utils.extensions.showHelp
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*
import kotlin.math.roundToLong

@Command("dice", "roll")
class Dice : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    private val random: Random = Random()

    private val dice = ShadowDice()

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (args.isEmpty()) {
            event.channel.sendMessage(GAME_DIE + " You rolled a `" + (dice.roll(20)) + "` on a **D20**.").queue(LuckyUser(event))
            return
        }

        if (args == "help") showHelp()

        try {
            event.channel.sendMessage(GAME_DIE + " You rolled `" + DiceEvaluator(args).parse().toPrettyString() + "`").queue(LuckyUser(event))
        } catch (_: Exception) {
            showHelp()
        }
    }

    private fun Double.toPrettyString(): String = if (this % 1 == 0.0) roundToLong().toString() else this.toString()

    override val helpHandler = HelpFactory("Dice Command") {
        aliases("roll")

        description("Have some fun, roll a dice.")
        examples(
            "dice d20",
            "dice 2d10",
            "dice 1d5 - 2",
            "dice 3d4 + 1d20",
            "dice d360 * pi"
        )
    }
}