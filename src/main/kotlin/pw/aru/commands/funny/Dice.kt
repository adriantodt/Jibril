package pw.aru.commands.funny

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.ICommand.CustomHandler.Result
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.Description
import pw.aru.bot.commands.help.Example
import pw.aru.bot.commands.help.Help
import pw.aru.commands.funny.dice.AruDice
import pw.aru.libs.dicenotation.exceptions.EvaluationException
import pw.aru.libs.dicenotation.exceptions.SyntaxException
import pw.aru.utils.extensions.discordapp.safeUserInput
import pw.aru.utils.extensions.discordapp.stripFormatting
import pw.aru.utils.text.GAME_DIE

@Command("dice", "roll")
class Dice : ICommand.Discrete, ICommand.HelpDialogProvider, ICommand.CustomHandler, ICommand.CustomDiscreteHandler {
    private val dicePattern = Regex("^\\d*d\\d+", RegexOption.IGNORE_CASE)
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
        send("$GAME_DIE **${author.effectiveName().safeUserInput()}**, ${resolveRoll(args)}")
    }

    override fun CommandContext.customCall(command: String): Result {
        if (!dicePattern.containsMatchIn(command)) return Result.IGNORE

        send("$GAME_DIE **${author.effectiveName().safeUserInput()}**, ${resolveRoll("$command $args")}".trim())

        return Result.HANDLED
    }

    override fun CommandContext.customCall(command: String, outer: String): Result {
        if (!dicePattern.containsMatchIn(command)) return Result.IGNORE

        val toSend = outer.replace('\n', ' ').stripFormatting().trim()

        if (toSend.isEmpty()) {
            send("$GAME_DIE **${author.effectiveName().safeUserInput()}**, ${resolveRoll("$command $args".trim())}")
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
        CommandDescription(
            listOf("dice", "roll"),
            "Dice Command",
            thumbnail = "https://assets.aru.pw/img/category/fun.png"
        ),
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