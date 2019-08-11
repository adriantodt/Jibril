@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.commands.funny.dice

import pw.aru.utils.AruTaskExecutor.schedule
import java.lang.System.nanoTime
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

class ShadowDice {
    val r = Random()

    init {
        schedule(65536, MILLISECONDS) { r.setSeed(3447679086515839964L xor nanoTime()) }
    }

    fun roll(sides: Int) = r.run {
        ((nextDouble() * 0.9 + nextDouble() * nextDouble() * nextDouble() * 0.9) * sides)
            .toInt().coerceIn(0, sides - 1).plus(1)
    }
}
