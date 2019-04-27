package pw.aru.commands.funny

import pw.aru.commands.funny.dice.AruDice
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.CustomHandler.Result
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Example
import pw.aru.core.commands.help.Help
import pw.aru.libs.dicenotation.exceptions.EvaluationException
import pw.aru.libs.dicenotation.exceptions.SyntaxException
import pw.aru.utils.extensions.discordapp.stripFormatting
import pw.aru.utils.text.GAME_DIE

@Command("dice", "roll")
class Dice : ICommand, ICommand.Discrete, ICommand.HelpDialogProvider, ICommand.CustomHandler,
    ICommand.CustomDiscreteHandler {
    private val dicePattern = Regex("\\d+?[Dd]\\d+")
    override val category = Category.FUN

    fun resolveRoll(args: String, simple: Boolean = false): String {
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
        send("$GAME_DIE **${author.effectiveName()}**, ${resolveRoll(args)}")
    }

    override fun CommandContext.customCall(command: String): Result {
        if (dicePattern.matchEntire(command) == null) return Result.IGNORE

        send("$GAME_DIE **${author.effectiveName()}**, ${resolveRoll(command + args)}")

        return Result.HANDLED
    }

    override fun CommandContext.customCall(command: String, outer: String): Result {
        if (dicePattern.matchEntire(command) == null) return Result.IGNORE

        val toSend = outer.replace('\n', ' ').stripFormatting().trim()

        if (toSend.isEmpty()) {
            send("$GAME_DIE **${author.effectiveName()}**, ${resolveRoll(command + args)}")
        } else {
            send("**$toSend**\n$GAME_DIE ${resolveRoll(args)}")
        }
        return Result.HANDLED
    }

    override fun CommandContext.discreteCall(outer: String) {
        val toSend = outer.replace('\n', ' ').stripFormatting().trim()

        if (toSend.isEmpty()) return call()

        send("**$toSend**\n$GAME_DIE ${resolveRoll(args)}")
    }

    override val helpHandler = Help(
        CommandDescription(listOf("dice", "roll"), "Dice Command", thumbnail = "https://assets.aru.pw/img/category/fun.png"),
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