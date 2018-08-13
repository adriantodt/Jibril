@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.commands.funny.dice

import pw.aru.utils.extensions.threadLocalRandom
import kotlin.math.max
import kotlin.math.min

class ShadowDice {
    private inline fun Int.clamp(min: Int, max: Int) = min(max(this, min), max)

    fun roll(sides: Int) = threadLocalRandom().run {
        ((nextDouble() * 0.9 + nextDouble() * nextDouble() * nextDouble() * 0.8) * sides).toInt().clamp(0, sides - 1) + 1
    }
}
