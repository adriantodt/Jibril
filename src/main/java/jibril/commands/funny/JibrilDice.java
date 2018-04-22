package jibril.commands.funny;

import jibril.dice.ast.Expr;
import jibril.dice.evaluator.DiceEvaluatorBuilder;
import jibril.dice.evaluator.DicePreEvaluator;
import jibril.dice.evaluator.DiceSolver;
import jibril.dice.lexer.DiceLexer;
import jibril.dice.parser.DiceParser;
import jibril.utils.extensions.KtExtensionsKt;

public class JibrilDice {
    private static DiceEvaluatorBuilder builder = new DiceEvaluatorBuilder();
    private static ShadowDice dice = new ShadowDice();

    static {
        builder
            .value("pi", Math.PI)
            .value("e", Math.E)
            .value("r", Math::random)
            .valueAlias("r", "rand", "rdn", "random")
            .function("sin", args -> Math.sin(args[0].doubleValue()))
            .function("cos", args -> Math.cos(args[0].doubleValue()))
            .function("tan", args -> Math.tan(args[0].doubleValue()))
            .function("random", args -> dice.roll(args[0].intValue()))
            .function("any", KtExtensionsKt::random)
            .functionAlias("random", "rand", "rdn", "r")
            .functionAlias("sin", "sen");
    }

    public static String execute(String s) {
        DiceLexer lexer = new DiceLexer(s);
        Expr rawExpr = new DiceParser(lexer).parse();
        Expr solvedExpr = rawExpr.accept(new DiceSolver(dice::roll));
        Expr preEvaluatedExpr = solvedExpr.accept(DicePreEvaluator.INSTANCE);
        Number result = solvedExpr.accept(builder.build());

        String solved = solvedExpr.toString();
        String preEvaluated = preEvaluatedExpr.toString();

        if (solved.equals(preEvaluated)) {
            return result + " ⟵ " + solved;
        }

        return result + " ⟵ " + preEvaluated + " ⟵ " + solved;
    }

    public static Number resolve(String s) {
        return new DiceParser(new DiceLexer(s))
            .parse()
            .accept(new DiceSolver(dice::roll).andFinally(builder.build()));
    }
}
