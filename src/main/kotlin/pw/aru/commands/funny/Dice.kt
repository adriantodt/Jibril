package pw.aru.commands.funny

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.funny.dice.AruDice
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.dice.exceptions.EvaluationException
import pw.aru.dice.exceptions.SyntaxException
import pw.aru.utils.DiscordUtils.stripFormatting
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.GAME_DIE
import kotlin.math.roundToLong

@Command("dice", "roll")
class Dice : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category = Categories.FUN

    private fun resolveRoll(args: String, simple: Boolean = false): String {
        when {
            args.startsWith("-simple") -> return resolveRoll(args.substring(7), true)
            args.endsWith("-simple") -> return resolveRoll(args.substring(0, args.length - 7), true)
            args.isBlank() -> return resolveRoll("d20", simple)
        }

        return try {
            if (simple) AruDice.resolve(args).toPrettyString() else AruDice.execute(args)
        } catch (e: Exception) {
            when (e) {
                is SyntaxException,
                is EvaluationException,
                is IllegalArgumentException,
                is IllegalStateException -> "Error: ${e.message}"
                else -> throw e
            }
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

