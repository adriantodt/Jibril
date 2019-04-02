package pw.aru.core.parser

fun Args.tryTakeInt(): Int? = mapNextString { it.toIntOrNull().let { v -> v to (v != null) } }

fun Args.takeInt(): Int = tryTakeInt() ?: throw IllegalStateException("argument is not a Int")

fun Args.takeInts() = deconstructed(Args::takeInt)

fun Args.takeAllInts(): List<Int> = generateSequence(this::tryTakeInt).toList()

fun Args.tryTakeLong(): Long? = mapNextString { it.toLongOrNull().let { v -> v to (v != null) } }

fun Args.takeLong(): Long = tryTakeLong() ?: throw IllegalStateException("argument is not a Long")

fun Args.takeLongs() = deconstructed(Args::takeLong)

fun Args.takeAllLongs(): List<Long> = generateSequence(this::tryTakeLong).toList()

fun Args.tryTakeFloat(): Float? = mapNextString { it.toFloatOrNull().let { v -> v to (v != null) } }

fun Args.takeFloat(): Float = tryTakeFloat() ?: throw IllegalStateException("argument is not a Float")

fun Args.takeFloats() = deconstructed(Args::takeFloat)

fun Args.takeAllFloats(): List<Float> = generateSequence(this::tryTakeFloat).toList()

fun Args.tryTakeDouble(): Double? = mapNextString { it.toDoubleOrNull().let { v -> v to (v != null) } }

fun Args.takeDouble(): Double = tryTakeDouble() ?: throw IllegalStateException("argument is not a Double")

fun Args.takeDoubles() = deconstructed(Args::takeDouble)

fun Args.takeAllDoubles(): List<Double> = generateSequence(this::tryTakeDouble).toList()

fun Args.tryTakeBoolean(): Boolean? = matchFirst(true to "true"::equals, false to "false"::equals)

fun Args.takeBoolean(): Boolean = tryTakeBoolean() ?: throw IllegalStateException("argument is not a Boolean")

fun Args.takeBooleans() = deconstructed(Args::takeBoolean)

fun Args.takeAllBooleans(): List<Boolean> = generateSequence(this::tryTakeBoolean).toList()
