package pw.aru.utils

import pw.aru.utils.AruTaskExecutor.schedule
import java.lang.Math.round
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

open class StatsManager<T> {
    companion object {
        private const val ACTIVE_BLOCK = "⬜"
        private const val EMPTY_BLOCK = "⬛"

        fun bar(percent: Int, total: Int): String {
            val activeBlocks = (percent / 100f * total).toInt()
            return (0 until total).joinToString("") { if (activeBlocks >= it) ACTIVE_BLOCK else EMPTY_BLOCK }
        }
    }

    val total = ConcurrentHashMap<T, AtomicLong>()
    val day = ConcurrentHashMap<T, AtomicLong>()
    val hour = ConcurrentHashMap<T, AtomicLong>()
    val minute = ConcurrentHashMap<T, AtomicLong>()

    enum class Type(val display: String) {
        TOTAL("Total") {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicLong> = m.total
        },
        DAY("Today") {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicLong> = m.day
        },
        HOUR("This Hour") {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicLong> = m.hour
        },
        MINUTE("Now") {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicLong> = m.minute
        };

        internal abstract operator fun <T> get(m: StatsManager<T>): Map<T, AtomicLong>
    }

    fun takeSnapshot(type: Type): StatSnapshot<T> {
        val items = type[this].toMap()
        val sum = items.values.map(AtomicLong::get).sum()

        return StatSnapshot(sum, items)
    }

    fun log(obj: T) {
        minute.computeIfAbsent(obj) { AtomicLong() }.incrementAndGet()
        schedule(1, TimeUnit.MINUTES) {
            if (minute[obj]?.decrementAndGet() == 0L) minute.remove(obj)
        }

        hour.computeIfAbsent(obj) { AtomicLong() }.incrementAndGet()
        schedule(1, TimeUnit.HOURS) {
            if (hour[obj]?.decrementAndGet() == 0L) hour.remove(obj)
        }

        day.computeIfAbsent(obj) { AtomicLong() }.incrementAndGet()
        schedule(1, TimeUnit.DAYS) {
            if (day[obj]?.decrementAndGet() == 0L) day.remove(obj)
        }

        total.computeIfAbsent(obj) { AtomicLong() }.incrementAndGet()
    }
}

data class StatSnapshot<T>(
    val sum: Long,
    val items: Map<T, AtomicLong>
) {
    fun resume(): String {
        return if (sum == 0L)
            "Nothing here, just dust."
        else
            "Total: $sum\n" + items.entries.asSequence()
                .map { it.key to it.value.get() }
                .filter { it.second > 0 }
                .sortedByDescending(Pair<T, Long>::second)
                .take(10)
                .joinToString("\n") { (k, v) ->
                    val p = round(v * 100.0f / sum)
                    "${StatsManager.bar(p, 5)} **$k** - $p% ($v)"
                }
    }
}

object CommandStatsManager : StatsManager<String>()

enum class GuildEvent { JOIN, LEAVE }

object GuildStatsManager : StatsManager<GuildEvent>()