@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

val environment = object : ReadOnlyProperty<Any?, String> {
    private val env by lazy { System.getenv() }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return env.get(property.name) ?: throw IllegalStateException("No environment property ${property.name}")
    }
}

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

private fun <T> CompletionStage<T>.stageToFuture(): CompletableFuture<T> {
    return if (this is CompletableFuture<*>) {
        this as CompletableFuture<T>
    } else {
        toCompletableFuture()
    }
}

fun <T> Array<CompletionStage<T>>.awaitAll(): CompletionStage<Void> {
    return CompletableFuture.allOf(
        *map { it.stageToFuture() }
            .toTypedArray()
    )
}

fun <T> Collection<CompletionStage<T>>.awaitAll(): CompletionStage<Void> {
    return CompletableFuture.allOf(
        *map { it.stageToFuture() }
            .toTypedArray()
    )
}

inline fun <K, V> Map<K, V>.ifContains(k: K, function: (V) -> Unit) {
    if (containsKey(k)) function(get(k)!!)
}

inline fun anyOf(vararg cases: Boolean) = cases.find { it } ?: false

inline fun allOf(vararg cases: Boolean) = cases.all { it }

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


fun threadGroupBasedFactory(name: String): (Runnable) -> Thread {
    val group = ThreadGroup(name)
    val count = AtomicLong(0)

    return {
        object : Thread(group, "$name-${count.getAndIncrement()}") {
            override fun run() {
                it.run()
            }
        }
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


inline fun <E> List<E>.random(): E = this[randomIndex(this.size)]

inline fun <E> List<E>.randomOrNull(): E? = this.getOrNull(randomIndex(this.size))

inline fun <E> Array<E>.random(): E = this[randomIndex(this.size)]

inline fun <E> Array<E>.randomOrNull(): E? = this.getOrNull(randomIndex(this.size))

inline fun <E> randomOf(vararg objects: E): E = objects.random()

@PublishedApi
internal inline fun randomIndex(max: Int): Int {
    return if (max == 0) 0 else Random.nextInt(max)
}

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
