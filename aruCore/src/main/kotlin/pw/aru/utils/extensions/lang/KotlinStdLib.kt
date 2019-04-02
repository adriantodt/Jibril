@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import java.util.concurrent.CompletionStage
import java.util.concurrent.Future
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.reflect.KProperty

inline fun <reified T> classOf() = T::class.java

@JvmName("futureGet")
inline operator fun <V> Future<V>.invoke(): V = get()

@JvmName("completionGet")
inline operator fun <V> CompletionStage<V>.invoke(): V = toCompletableFuture().get()

@JvmName("futureCompletionGet")
inline operator fun <V, T> T.invoke(): V where T : Future<V>, T : CompletionStage<V> = get()


@JvmName("futureGetValue")
inline operator fun <V> Future<V>.getValue(r: Any?, p: KProperty<*>): V = get()

@JvmName("completionGetValue")
inline operator fun <V> CompletionStage<V>.getValue(r: Any?, p: KProperty<*>): V = toCompletableFuture().join()

@JvmName("futureCompletionGetValue")
inline operator fun <V, T> T.getValue(r: Any?, p: KProperty<*>): V where T : Future<V>, T : CompletionStage<V> = get()


inline fun <K, V> Map<K, V>.ifContains(k: K, function: (V) -> Unit) {
    if (containsKey(k)) function(get(k)!!)
}

inline fun anyOf(vararg cases: Boolean) = cases.find { it } ?: false

inline fun multiline(vararg lines: String) = lines.joinToString("\n")

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

inline fun <T> Semaphore.acquiring(permits: Int = 1, run: () -> T): T {
    acquire(permits)
    try {
        return run()
    } finally {
        release(permits)
    }
}

inline fun <T, U> T.applyOn(thisObj: U, block: U.() -> Unit): T {
    thisObj.block()
    return this
}

inline fun <T> handlers(
    crossinline success: (T) -> Unit = {},
    crossinline failure: (Throwable) -> Unit = {}
): (T, Throwable?) -> Unit = { obj, t ->
    if (t != null) {
        failure(t)
    } else {
        success(obj)
    }
}

inline fun Any.format(s: String): String = s.format(this)

inline operator fun Appendable.plusAssign(other: CharSequence) {
    append(other)
}

inline operator fun Appendable.plusAssign(other: Char) {
    append(other)
}


inline fun <E> List<E>.random(): E = this[Random.nextInt(this.size)]

inline fun <E> List<E>.randomOrNull(): E? = this.getOrNull(Random.nextInt(this.size))

inline fun <E> Array<E>.random(): E = this[Random.nextInt(this.size)]

inline fun <E> Array<E>.randomOrNull(): E? = this.getOrNull(Random.nextInt(this.size))

inline fun <E> randomOf(vararg objects: E): E = objects.random()

fun <T> Iterable<Iterable<T>>.roundRobinFlatten(): List<T> {
    val result = ArrayList<T>()
    val iterators = mapTo(ArrayList(), Iterable<T>::iterator)
    val toRemove = ArrayList<Iterator<T>>()

    while (iterators.isNotEmpty()) {
        for (iterator in iterators) {
            if (iterator.hasNext()) {
                result.add(iterator.next())
            } else {
                toRemove.add(iterator)
            }
        }
        iterators.removeAll(toRemove)
        toRemove.clear()
    }

    return result
}
