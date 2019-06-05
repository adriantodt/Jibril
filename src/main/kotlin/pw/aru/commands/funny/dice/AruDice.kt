package pw.aru.commands.funny.dice

import pw.aru.libs.dicenotation.evaluator.DiceEvaluatorBuilder
import pw.aru.libs.dicenotation.evaluator.DiceSolver
import pw.aru.libs.dicenotation.lexer.DiceLexer
import pw.aru.libs.dicenotation.parser.DiceParser
import pw.aru.utils.extensions.lang.randomOf

class AruDice(text: String) {
    companion object {
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
            .function("average") { sequenceOf(*it).map(Number::toDouble).average() }
            .function("any") { randomOf(*it) }
            .function("int") { it[0].toInt() }
            .function("double") { it[0].toDouble() }
            .functionAlias("random", "rand", "rdn", "r", "roll")
            .functionAlias("average", "avg")
            .functionAlias("sin", "sen")
            .functionAlias("int", "integer", "long")
            .functionAlias("double", "float", "decimal")
            .build()
        private val solver = DiceSolver(dice::roll)
    }

    val diceExpr = DiceParser(DiceLexer(text)).parse().accept(solver)!!
    val solvedValue = diceExpr.accept(evaluator)!!

    private val diceText by lazy(diceExpr::toString)
    private val valueText by lazy(solvedValue::toString)

    fun getText(simple: Boolean = false): String {
        if (simple) return valueText
        val text = "$valueText ⟵ $diceText"
        return if (text.length > 1900) "$valueText (Output simplified for being too long)" else text
    }
}
