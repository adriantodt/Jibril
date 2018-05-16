package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.dice.exceptions.EvaluationException
import jibril.dice.exceptions.SyntaxException
import jibril.utils.DiscordUtils.stripFormatting
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.GAME_DIE
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import kotlin.math.roundToLong

@Command("dice", "roll")
class Dice : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {

    override val category = Categories.FUN

    fun resolveRoll(args: String, simple: Boolean = false): String {
        when {
            args.startsWith("-simple") -> return resolveRoll(args.substring(7), true)
            args.endsWith("-simple") -> return resolveRoll(args.substring(0, args.length - 7), true)
            args.isBlank() -> return resolveRoll("d20", simple)
        }

        return try {
            if (simple) JibrilDice.resolve(args).toPrettyString() else JibrilDice.execute(args)
        } catch (e: SyntaxException) {
            "Error: ${e.message}"
        } catch (e: EvaluationException) {
            "Error: ${e.message}"
        }
    }

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        event.channel.sendMessage("$GAME_DIE **${event.member.effectiveName}**, ${resolveRoll(args)}").queue()
    }

    override fun discreteCall(event: GuildMessageReceivedEvent, args: String, outer: String) {
        val toSend = stripFormatting(outer.replace('\n', ' '))

        event.channel.sendMessage("**$toSend**\n$GAME_DIE ${resolveRoll(args)}").queue()
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

