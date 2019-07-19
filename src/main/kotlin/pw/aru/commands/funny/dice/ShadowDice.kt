@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.commands.funny.dice

import pw.aru.utils.AruTaskExecutor.schedule
import java.lang.System.nanoTime
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.math.max
import kotlin.math.min

class ShadowDice {
    val r = Random()

    init {
        schedule(65536, MILLISECONDS) { r.setSeed(3447679086515839964L xor nanoTime()) }
    }

    private inline fun Int.clamp(min: Int, max: Int) = min(max(this, min), max)

    fun roll(sides: Int) = r.run {
        ((nextDouble() * 0.9 + nextDouble() * nextDouble() * nextDouble() * 0.8) * sides).toInt()
            .clamp(0, sides - 1).plus(1)
    }
}
