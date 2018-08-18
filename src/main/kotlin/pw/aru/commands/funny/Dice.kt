package pw.aru.commands.funny

import pw.aru.commands.funny.dice.AruDice
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Example
import pw.aru.core.commands.help.Help
import pw.aru.dice.exceptions.EvaluationException
import pw.aru.dice.exceptions.SyntaxException
import pw.aru.utils.emotes.GAME_DIE
import pw.aru.utils.extensions.stripFormatting

@Command("dice", "roll")
class Dice : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider {
    override val category = Category.FUN

    private fun resolveRoll(args: String, simple: Boolean = false): String {
        when {
            args.startsWith("-simple") -> return resolveRoll(args.substring(7), true)
            args.endsWith("-simple") -> return resolveRoll(args.substring(0, args.length - 7), true)
            args.isBlank() -> return resolveRoll("d20", simple)
        }

        return try {
            AruDice(args).getText(simple)
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

    override fun CommandContext.call() {
        send("$GAME_DIE **${event.member.effectiveName}**, ${resolveRoll(args)}").queue()
    }

    override fun CommandContext.discreteCall(outer: String) {
        val toSend = outer.replace('\n', ' ').stripFormatting().trim()

        if (toSend.isEmpty()) return call()

        send("**$toSend**\n$GAME_DIE ${resolveRoll(args)}").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("dice", "roll"), "Dice Command"),
        Description(
            "Rolls a dice, which needs to be written in dice notation.",
            "[Click here to learn more about dice notation.](https://aru.pw/features/dicenotation)"
        ),
        Example(
            "dice d20",
            "dice 2d10",
            "dice 1d5 - 2",
            "dice 3d4 + 1d20",
            "dice d360 * pi"
        )
    )
}

