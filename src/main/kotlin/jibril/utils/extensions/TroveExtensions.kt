@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import gnu.trove.map.TLongObjectMap

inline fun <T> TLongObjectMap<T>.computeIfAbsent(key: Long, value: (Long) -> T): T {
    if (!containsKey(key)) {
        val t = value(key)
        put(key, t)
        return t
    }
    return get(key)
}

inline fun <T> TLongObjectMap<T>.getOrDefault(key: Long, value: T): T {
    return if (!containsKey(key)) {
        value
    } else get(key)
}