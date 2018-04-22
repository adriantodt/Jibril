package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.GAME_DIE
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import kotlin.math.roundToLong

@Command("dice", "roll")
class Dice : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    override fun call(event: GuildMessageReceivedEvent, args: String) = roll(event, args, false)

    fun roll(event: GuildMessageReceivedEvent, args: String, simple: Boolean) {
        when {
            args.startsWith("-simple") -> return roll(event, args.substring(7), true)
            args.endsWith("-simple") -> return roll(event, args.substring(0, args.length - 7), true)
            args.isBlank() -> return roll(event, "d20", simple)
        }

        event.channel.sendMessage(
            "$GAME_DIE **${event.member.effectiveName}**, you rolled ${
            if (simple) JibrilDice.resolve(args).toPrettyString() else JibrilDice.execute(args)
            }"
        ).queue()
    }

    private fun Number.toPrettyString(): String = when (this) {
        is Double -> {
            if (this % 1 == 0.0) roundToLong().toString() else this.toString()
        }
        is Float -> {
            if (this % 1 == 0f) roundToLong().toString() else this.toString()
        }
        else -> {
            toString()
        }
    }

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

