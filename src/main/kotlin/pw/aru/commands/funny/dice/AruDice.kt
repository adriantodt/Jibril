package pw.aru.commands.funny.dice

import jibril.dice.ast.value.DecimalNode
import jibril.dice.ast.value.IntNode
import jibril.dice.ast.value.SolvedDiceNode
import jibril.dice.evaluator.DiceEvaluatorBuilder
import jibril.dice.evaluator.DicePreEvaluator
import jibril.dice.evaluator.DiceSolver
import jibril.dice.lexer.DiceLexer
import jibril.dice.parser.DiceParser
import pw.aru.utils.extensions.randomOf
import java.util.function.IntUnaryOperator

object AruDice {
    private val builder = DiceEvaluatorBuilder()
    private val dice = ShadowDice()

    init {
        builder
            .value("pi", Math.PI)
            .value("e", Math.E)
            .value("r", Math::random)
            .valueAlias("r", "rand", "rdn", "random")
            .function("sin") { Math.sin(it[0].toDouble()) }
            .function("cos") { Math.cos(it[0].toDouble()) }
            .function("tan") { Math.tan(it[0].toDouble()) }
            .function("random") { dice.roll(it[0].toInt()) }
            .function("any") { randomOf(*it) }
            .functionAlias("random", "rand", "rdn", "r")
            .functionAlias("sin", "sen")
    }

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
                val result = solvedExpr.accept(builder.build())
                "$result ⟵ $preEvaluatedExpr ⟵ $solvedExpr"
            }
        }
    }

    fun resolve(s: String): Number {
        return DiceParser(DiceLexer(s))
            .parse()
            .accept(DiceSolver(IntUnaryOperator { dice.roll(it) }).andFinally(builder.build()))
    }
}
