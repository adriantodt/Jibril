package pw.aru.commands.funny.dice

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class ShadowDice {
    private fun Int.clamp(min: Int, max: Int) = min(max(this, min), max)
    private fun Double.clamp(min: Double, max: Double) = min(max(this, min), max)

    fun roll(sides: Int): Int {
        val random = ThreadLocalRandom.current()

        return ((random.nextDouble() * 1.1 + random.nextGaussian() * 0.3) * sides).toInt().clamp(0, sides - 1) + 1
    }
}
