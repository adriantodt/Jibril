@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.commands.funny.dice

import java.util.*
import kotlin.math.max
import kotlin.math.min

class ShadowDice {
    val r = Random()

    private inline fun Int.clamp(min: Int, max: Int) = min(max(this, min), max)

    fun roll(sides: Int) = r.run {
        ((nextDouble() * 0.9 + nextDouble() * nextDouble() * nextDouble() * 0.8) * sides).toInt().clamp(0, sides - 1) + 1
    }
}
