package pw.aru.utils.helpers

import net.dv8tion.jda.core.EmbedBuilder
import pw.aru.utils.TaskManager.schedule
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

open class StatsManager<T> {
    companion object {
        private const val ACTIVE_BLOCK = '\u2588'
        private const val EMPTY_BLOCK = '\u200b'

        fun bar(percent: Int, total: Int): String {
            val activeBlocks = (percent.toFloat() / 100f * total).toInt()
            val builder = StringBuilder().append('`').append(EMPTY_BLOCK)
            for (i in 0 until total) builder.append(if (activeBlocks > i) ACTIVE_BLOCK else ' ')
            return builder.append(EMPTY_BLOCK).append('`').toString()
        }
    }

    val total = ConcurrentHashMap<T, AtomicInteger>()
    val day = ConcurrentHashMap<T, AtomicInteger>()
    val hour = ConcurrentHashMap<T, AtomicInteger>()
    val minute = ConcurrentHashMap<T, AtomicInteger>()

    enum class Type {
        TOTAL {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicInteger> = m.total
        },
        DAY {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicInteger> = m.day
        },
        HOUR {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicInteger> = m.hour
        },
        MINUTE {
            override fun <T> get(m: StatsManager<T>): Map<T, AtomicInteger> = m.minute
        };

        internal abstract operator fun <T> get(m: StatsManager<T>): Map<T, AtomicInteger>
    }

    fun fillEmbed(builder: EmbedBuilder, type: Type) {
        val commands = type[this]

        val total = commands.values.map(AtomicInteger::get).sum()

        if (total == 0) {
            builder.addField("Nothing Here.", "Just Dust.", false)
            return
        }

        commands.entries
            .map { it.key to it.value.get() }
            .filter { it.second > 0 }
            .sortedByDescending(Pair<T, Int>::second)
            .take(12)
            .forEach { (k, v) ->
                val percent = v * 100 / total
                builder.addField(
                    k.toString(), "${bar(percent, 15)} $percent% ($v)", true
                )
            }
    }

    fun log(obj: T) {
        minute.computeIfAbsent(obj) { AtomicInteger() }.incrementAndGet()
        schedule(1, TimeUnit.MINUTES) {
            if (minute[obj]?.decrementAndGet() == 0) minute.remove(obj)
        }

        hour.computeIfAbsent(obj) { AtomicInteger() }.incrementAndGet()
        schedule(1, TimeUnit.HOURS) {
            if (hour[obj]?.decrementAndGet() == 0) hour.remove(obj)
        }

        day.computeIfAbsent(obj) { AtomicInteger() }.incrementAndGet()
        schedule(1, TimeUnit.DAYS) {
            if (day[obj]?.decrementAndGet() == 0) day.remove(obj)
        }

        total.computeIfAbsent(obj) { AtomicInteger() }.incrementAndGet()
    }

    fun resume(type: Type): String {
        val commands = type[this]

        val total = commands.values.map(AtomicInteger::get).sum()

        return if (total == 0)
            "Nothing here, just dust."
        else
            "Total: $total\n" + commands.entries
                .map { it.key to it.value.get() }
                .filter { it.second > 0 }
                .sortedByDescending(Pair<T, Int>::second)
                .take(5)
                .joinToString("\n") { (k, v) ->
                    val percent = Math.round(v.toFloat() * 100 / total)
                    "${bar(percent, 15)} $percent% **$k** ($v)"
                }
    }
}

object CommandStatsManager : StatsManager<String>()

enum class GuildEvent { JOIN, LEAVE }

object GuildStatsManager : StatsManager<GuildEvent>()