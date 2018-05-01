@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import redis.clients.util.Pool
import java.io.Closeable
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

inline fun <reified T> classOf() = T::class.java

inline operator fun <V> Future<V>.invoke(): V = get()

inline fun <K, V> Map<K, V>.ifContains(k: K, function: (V) -> Unit) {
    if (containsKey(k)) function(get(k)!!)
}

inline fun Any.format(s: String): String = s.format(this)

inline operator fun Appendable.plusAssign(other: CharSequence) {
    append(other)
}

inline operator fun Appendable.plusAssign(other: Char) {
    append(other)
}

inline operator fun <T> Consumer<T>.invoke(it: T) = accept(it)

inline fun threadLocalRandom(): ThreadLocalRandom = ThreadLocalRandom.current()

inline fun <E> List<E>.random(random: Random = threadLocalRandom()): E = this[random.nextInt(this.size)]

inline fun <E> Array<E>.random(random: Random = threadLocalRandom()): E = this[random.nextInt(this.size)]

inline fun <E> randomOf(vararg objects: E): E = objects.random()

inline fun <E> Random.anyOf(vararg objects: E): E = objects.random(this)

inline fun <T : Closeable?, R> Pool<T>.useResource(block: (T) -> R) = resource.use(block)
