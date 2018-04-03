package jibril.utils

import java.util.Objects.hash
import java.util.concurrent.TimeUnit

data class TimeAmount(val amount: Long, val unit: TimeUnit) {

    override fun toString() = "$amount ${unit.toString().toLowerCase()}"

    fun compress(): TimeAmount {
        return this[timeUnits.last {
            //If do back and forth conversion is lossless
            unit.convert(it.convert(amount, unit), it) == amount
        }]
    }

    infix operator fun get(newUnit: TimeUnit): TimeAmount {
        return if (unit == newUnit) this else TimeAmount(newUnit.convert(amount, unit), newUnit)
    }

    @Throws(InterruptedException::class)
    fun sleep() = unit.sleep(amount)

    @Throws(InterruptedException::class)
    fun Thread.timedJoin() = unit.timedJoin(this, amount)

    @Throws(InterruptedException::class)
    fun Any.timedWait() = unit.timedWait(this, amount)

    fun toDays() = unit.toDays(amount)

    fun toHours() = unit.toHours(amount)

    fun toMicros() = unit.toMicros(amount)

    fun toMillis() = unit.toMillis(amount)

    fun toMinutes() = unit.toMinutes(amount)

    fun toNanos() = unit.toNanos(amount)

    fun toSeconds() = unit.toSeconds(amount)

    override fun equals(other: Any?): Boolean {
        return other is TimeAmount && other.compress() == this.compress()
    }

    override fun hashCode() = hash(amount, unit)

    companion object {
        private val timeUnits = TimeUnit.values()

        fun commonUnit(vararg amounts: TimeAmount) = amounts.minBy { it.unit }?.unit ?: timeUnits[0]

        fun normalize(vararg amounts: TimeAmount): Array<TimeAmount> {
            val unit = commonUnit(*amounts)
            return amounts.map { it[unit] }.toTypedArray()
        }

        fun sum(vararg amounts: TimeAmount): TimeAmount {
            val normal = normalize(*amounts.map(TimeAmount::compress).toTypedArray())
            return TimeAmount(normal.map(TimeAmount::amount).sum(), normal[0].unit).compress()
        }
    }
}
