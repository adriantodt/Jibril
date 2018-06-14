package pw.aru.commands.funny.dice

import pw.aru.dice.ast.value.DecimalNode
import pw.aru.dice.ast.value.IntNode
import pw.aru.dice.ast.value.SolvedDiceNode
import pw.aru.dice.evaluator.DiceEvaluatorBuilder
import pw.aru.dice.evaluator.DicePreEvaluator
import pw.aru.dice.evaluator.DiceSolver
import pw.aru.dice.lexer.DiceLexer
import pw.aru.dice.parser.DiceParser
import pw.aru.utils.extensions.randomOf
import java.util.function.IntUnaryOperator

object AruDice {
    private val dice = ShadowDice()
    private val evaluator = DiceEvaluatorBuilder()
        .value("pi", Math.PI)
        .value("e", Math.E)
        .value("r", Math::random)
        .valueAlias("r", "rand", "rdn", "random")
        .function("sin") { Math.sin(it[0].toDouble()) }
        .function("cos") { Math.cos(it[0].toDouble()) }
        .function("tan") { Math.tan(it[0].toDouble()) }
        .function("random") { dice.roll(it[0].toInt()) }
        .function("any") { randomOf(*it) }
        .function("int") { it[0].toInt() }
        .function("double") { it[0].toDouble() }
        .functionAlias("random", "rand", "rdn", "r")
        .functionAlias("sin", "sen")
        .functionAlias("int", "integer", "long")
        .functionAlias("double", "float", "decimal")
        .build()

    fun execute(s: String): String {
        val solvedExpr = DiceParser(DiceLexer(s))
            .parse()
            .accept(DiceSolver(dice::roll))

        val preEvaluatedExpr = solvedExpr.accept(DicePreEvaluator.INSTANCE)

        return when (preEvaluatedExpr) {
            is SolvedDiceNode, is IntNode, is DecimalNode -> {
                "$preEvaluatedExpr ⟵ $solvedExpr"
            }
            else -> {
                val result = solvedExpr.accept(evaluator)
                "$result ⟵ $solvedExpr"
            }
        }
    }

    fun resolve(s: String): Number {
        return DiceParser(DiceLexer(s))
            .parse()
            .accept(DiceSolver(IntUnaryOperator { dice.roll(it) }).andFinally(evaluator))
    }
}
