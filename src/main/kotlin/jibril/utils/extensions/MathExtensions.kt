@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import kotlin.experimental.xor
import kotlin.math.absoluteValue
import kotlin.math.exp

inline fun sigmoid(f: Double) = 1 / (1 + exp(-f))

inline fun sigmoid(f: Float) = 1 / (1 + exp(-f))

inline fun floor(d: Double, factor: Double = 1.0) = Math.floor(d * factor) / factor

inline fun floor(f: Float, factor: Float = 1.0f) = (Math.floor((f * factor).toDouble()) / factor).toFloat()

fun Long.toByteArray(): ByteArray = Longs.toByteArray(this)

fun Int.toByteArray(): ByteArray = Ints.toByteArray(this)

fun Long.fold(): Int {
    val bytes = Longs.toByteArray(this)
    return Ints.fromBytes(bytes[0], bytes[2], bytes[3], bytes[5]) xor Ints.fromBytes(bytes[1], bytes[3], bytes[5], bytes[7])
}

fun Int.fold(): Short {
    val bytes = Ints.toByteArray(this)
    return Shorts.fromBytes(bytes[0], bytes[2]) xor Shorts.fromBytes(bytes[1], bytes[3])
}

fun Short.fold(): Byte {
    val bytes = Shorts.toByteArray(this)
    return bytes[0] xor bytes[1]
}

val Short.absoluteValue: Short
    get() = toInt().absoluteValue.toShort()

val Byte.absoluteValue: Byte
    get() = toInt().absoluteValue.toByte()
