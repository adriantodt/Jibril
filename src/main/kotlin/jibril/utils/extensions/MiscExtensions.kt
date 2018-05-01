@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import com.google.inject.Injector
import jibril.snowflake.entities.SnowflakeDatacenter
import jibril.snowflake.entities.SnowflakeGenerator
import jibril.snowflake.entities.SnowflakeWorker

// Guice
inline operator fun <reified T> Injector.invoke(): T = this(classOf())

inline operator fun <T> Injector.invoke(c: Class<T>): T = getInstance(c)

// Snowflakes
inline operator fun SnowflakeGenerator.get(datacenter: Long, worker: Long): SnowflakeWorker = getWorker(datacenter, worker)

inline operator fun SnowflakeGenerator.get(datacenter: Long): SnowflakeDatacenter = getDatacenter(datacenter)
inline operator fun SnowflakeDatacenter.get(worker: Long): SnowflakeWorker = getWorker(worker)

// Misc
fun <E> Iterable<E>.toSmartString(transform: ((E) -> CharSequence)? = null): String {
    val list = toMutableList()

    if (list.isEmpty()) return "nothing"
    if (list.size == 1) return first().transformElement(transform).toString()
    if (list.size == 2) {
        val (e1, e2) = list
        return "${e1.transformElement(transform)} and ${e2.transformElement(transform)}"
    }
    val last = list.removeAt(list.size - 1)
    return list.joinToString(", ", transform = transform, postfix = " and ${last.transformElement(transform)}")
}

internal fun <T> T.transformElement(transform: ((T) -> CharSequence)?): CharSequence {
    return when {
        transform != null -> transform(this)
        this is CharSequence -> this
        else -> this.toString()
    }
}