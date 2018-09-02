@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import ch.qos.logback.core.helpers.ThrowableToStringArray
import redis.clients.util.Pool
import java.io.Closeable
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import kotlin.concurrent.thread

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

inline fun <E> List<E>.randomOrNull(random: Random = threadLocalRandom()): E? = if (isEmpty()) null else this[random.nextInt(this.size)]

inline fun <E> Array<E>.random(random: Random = threadLocalRandom()): E = this[random.nextInt(this.size)]

inline fun <E> Array<E>.randomOrNull(random: Random = threadLocalRandom()): E? = if (isEmpty()) null else this[random.nextInt(this.size)]

inline fun <E> randomOf(vararg objects: E): E = objects.random()

inline fun <E> Random.anyOf(vararg objects: E): E = objects.random(this)

inline fun <T : Closeable?, R> Pool<T>.useResource(block: (T) -> R) = resource.use(block)

inline fun Throwable.stackTraceToString() = ThrowableToStringArray.convert(this).joinToString("\n")

inline fun <T, U> T.applyOn(thisObj: U, block: U.() -> Unit): T {
    thisObj.block()
    return this
}

inline fun anyOf(vararg cases: Boolean) = cases.find { it } ?: false

fun threadFactory(
    isDaemon: Boolean = false,
    contextClassLoader: ClassLoader? = null,
    nameFormat: String? = null,
    priority: Int = -1
): ThreadFactory {
    val count = if (nameFormat != null) AtomicLong(0) else null
    return ThreadFactory {
        thread(false, isDaemon, contextClassLoader, nameFormat?.format(count!!.getAndIncrement()), priority, it::run)
    }
}
