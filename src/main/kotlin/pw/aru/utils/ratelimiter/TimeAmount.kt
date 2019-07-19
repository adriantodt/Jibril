package pw.aru.utils.ratelimiter

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.*

data class TimeAmount(val amount: Long, val unit: TimeUnit) {
    constructor(amount: Long) : this(amount, MILLISECONDS)

    fun toNanos() = NANOSECONDS.convert(amount, unit)
    fun toMicros() = MICROSECONDS.convert(amount, unit)
    fun toMillis() = MILLISECONDS.convert(amount, unit)
    fun toSeconds() = SECONDS.convert(amount, unit)
    fun toMinutes() = MINUTES.convert(amount, unit)
    fun toHours() = HOURS.convert(amount, unit)
    fun toDays() = DAYS.convert(amount, unit)

    infix fun to(outputUnit: TimeUnit) = TimeAmount(outputUnit.convert(amount, unit), outputUnit)
}