package pw.aru.utils

import pw.aru.utils.extensions.lang.toSmartString
import java.util.*
import java.util.concurrent.TimeUnit

fun humanizedTime(vararg pairs: Pair<Long, TimeUnit>): String {
    return humanizedTime(pairs.map { (time, unit) -> unit.toMillis(time) }.sum())
}

fun humanizedTime(millis: Long): String {
    val days = millis / 86400000
    val hours = millis / 3600000 % 24
    val minutes = millis / 60000 % 60
    val seconds = millis / 1000 % 60

    val parts = LinkedList<String>()

    if (days > 0) parts += "$days ${if (days == 1L) "day" else "days"}"
    if (hours > 0) parts += "$hours ${if (hours == 1L) "hour" else "hours"}"
    if (minutes > 0) parts += "$minutes ${if (minutes == 1L) "minute" else "minutes"}"
    if (seconds > 0) parts += "$seconds ${if (seconds == 1L) "second" else "seconds"}"

    return if (parts.isEmpty()) "0 seconds (about now...)" else parts.toSmartString()
}
