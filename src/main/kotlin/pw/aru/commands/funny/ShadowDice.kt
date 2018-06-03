package pw.aru.commands.funny

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class ShadowDice {
    private fun Int.clamp(min: Int, max: Int) = min(max(this, min), max)

    fun roll(sides: Int): Int {
        val random = ThreadLocalRandom.current()

        return ((random.nextDouble() * 0.75 + random.nextGaussian() * 0.25) * sides).toInt().clamp(0, sides - 1) + 1
    }
}
