package pw.aru.core.parser

fun Args.tryTakeInt(): Int? = mapNextString { it.toIntOrNull().toMapResult() }

fun Args.takeInt(): Int = tryTakeInt() ?: throw IllegalStateException("argument is not a Int")

fun Args.takeAllInts(): List<Int> = generateSequence(this::tryTakeInt).toList()

fun Args.tryTakeLong(): Long? = mapNextString { it.toLongOrNull().toMapResult() }

fun Args.takeLong(): Long = tryTakeLong() ?: throw IllegalStateException("argument is not a Long")

fun Args.takeAllLongs(): List<Long> = generateSequence(this::tryTakeLong).toList()

fun Args.tryTakeFloat(): Float? = mapNextString { it.toFloatOrNull().toMapResult() }

fun Args.takeFloat(): Float = tryTakeFloat() ?: throw IllegalStateException("argument is not a Float")

fun Args.takeAllFloats(): List<Float> = generateSequence(this::tryTakeFloat).toList()

fun Args.tryTakeDouble(): Double? = mapNextString { it.toDoubleOrNull().toMapResult() }

fun Args.takeDouble(): Double = tryTakeDouble() ?: throw IllegalStateException("argument is not a Double")

fun Args.takeAllDoubles(): List<Double> = generateSequence(this::tryTakeDouble).toList()

fun Args.tryTakeBoolean(): Boolean? = matchFirst(true to "true"::equals, false to "false"::equals)

fun Args.takeBoolean(): Boolean = tryTakeBoolean() ?: throw IllegalStateException("argument is not a Boolean")

fun Args.takeAllBooleans(): List<Boolean> = generateSequence(this::tryTakeBoolean).toList()
