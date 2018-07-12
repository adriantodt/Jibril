package pw.aru.core.parser

fun Args.tryTakeInt(): Int? = mapNextString {
    try {
        it.toInt() to true
    } catch (e: NumberFormatException) {
        null to false
    }
}

fun Args.takeInt(): Int = tryTakeInt() ?: throw IllegalStateException("argument is not a Int")

fun Args.takeInts() = descontructed(Args::takeInt)

fun Args.takeAllInts(): List<Int> {
    return generateSequence(this::tryTakeInt).toList()
}

fun Args.tryTakeLong(): Long? = mapNextString {
    try {
        it.toLong() to true
    } catch (e: NumberFormatException) {
        null to false
    }
}

fun Args.takeLong(): Long = tryTakeLong() ?: throw IllegalStateException("argument is not a Long")

fun Args.takeLongs() = descontructed(Args::takeLong)

fun Args.takeAllLongs(): List<Long> {
    return generateSequence(this::tryTakeLong).toList()
}

fun Args.tryTakeDouble(): Double? = mapNextString {
    try {
        it.toDouble() to true
    } catch (e: NumberFormatException) {
        null to false
    }
}

fun Args.takeDouble(): Double = tryTakeDouble() ?: throw IllegalStateException("argument is not a Double")

fun Args.takeDoubles() = descontructed(Args::takeDouble)

fun Args.takeAllDoubles(): List<Double> {
    return generateSequence(this::tryTakeDouble).toList()
}
