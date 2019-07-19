package pw.aru.core.parser

import java.util.concurrent.TimeUnit

private val letterBased = Regex("(\\d+[hms])+")
private val letterBasedReader = Regex("(\\d+)([hms])")
private val colonBased = Regex("(?:(\\d+))(?::(\\d+))?(?::(\\d+))?")
private val colonBasedReader = Regex("(?:(\\d+):?)")

fun Args.tryTakeTimeMillis(): Long? {
    return mapNextString {
        when {
            letterBased.matches(it) -> {
                letterBasedReader.findAll(it).map { m ->
                    val (i, type) = m.destructured
                    when (type) {
                        "h" -> TimeUnit.HOURS.toMillis(i.toLong())
                        "m" -> TimeUnit.MINUTES.toMillis(i.toLong())
                        "s" -> TimeUnit.SECONDS.toMillis(i.toLong())
                        else -> throw IllegalStateException("type is $type")
                    }
                }.sum().toMapResult()
            }
            colonBased.matches(it) -> {
                colonBasedReader.findAll(it).map { m -> m.groupValues[1].toLong() }.toList().asReversed()
                    .zip(TimeUnit.values().drop(3)) { v, u -> u.toMillis(v) }.sum().toMapResult()
            }
            else -> Args.MapResult<Long?>(null, false)
        }
    }
}

fun Args.takeTimeMillis(): Long = tryTakeTimeMillis() ?: throw IllegalStateException("argument is not a valid Time")
