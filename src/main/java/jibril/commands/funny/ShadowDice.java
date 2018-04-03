package jibril.commands.funny;

import java.util.Random;

public class ShadowDice {
    private final Random random = new Random(), shadow = new Random(random.nextLong());

    public int roll(int sides) {
        return Math.min(random.nextInt(sides) + (sides / 5 == 0 ? 0 : shadow.nextInt(sides / 5)), sides - 1) + 1;
    }
}
