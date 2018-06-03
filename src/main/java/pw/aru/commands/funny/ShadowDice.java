package pw.aru.commands.funny;

import java.util.concurrent.ThreadLocalRandom;

public class ShadowDice {

    private int clamp(int v, int min, int max) {
        return Math.min(Math.max(v, min), max);
    }

    public int roll(int sides) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        return clamp((int) ((random.nextDouble() * 0.75 + random.nextGaussian() * 0.25) * sides), 0, sides - 1) + 1;
    }
}
