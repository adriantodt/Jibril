@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import ch.qos.logback.core.helpers.ThrowableToStringArray


fun <E> List<E>.split(minSize: Int, maxSize: Int): List<List<E>> {
    return when {
        size < maxSize -> listOf(this)
        else -> chunked((minSize..maxSize).minBy { it - size % it } ?: (minSize + maxSize) / 2)
    }
}

fun Throwable.simpleName(): String {
    var c: Class<*>? = javaClass

    while (c != null) {
        val name = c.simpleName
        if (!name.isEmpty()) return name
        c = c.superclass
    }

    return "Throwable"
}

fun Throwable.especializationName(): String {
    var c: Class<*>? = javaClass

    while (c != null) {
        val name = c.simpleName
        if (!name.isEmpty()) {
            return when {
                name.endsWith("Exception") -> name.substring(0, name.length - 9)
                name.endsWith("Error") -> name.substring(0, name.length - 5)
                else -> name
            }
        }
        c = c.superclass
    }

    return "Throwable"
}

fun Throwable.stackTraceToString() = ThrowableToStringArray.convert(this).joinToString("\n")
