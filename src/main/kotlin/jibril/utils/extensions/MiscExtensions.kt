package jibril.utils.extensions

import br.com.brjdevs.java.snowflakes.entities.Config
import br.com.brjdevs.java.snowflakes.entities.Datacenter
import br.com.brjdevs.java.snowflakes.entities.Worker
import com.google.inject.Injector

//Guice
inline operator fun <reified T> Injector.invoke(): T = this(classOf())

operator fun <T> Injector.invoke(c: Class<T>): T = getInstance(c)

operator fun Config.get(datacenterId: Long, workerId: Long): Worker = worker(datacenterId, workerId)
operator fun Config.get(datacenterId: Long): Datacenter = datacenter(datacenterId)
operator fun Datacenter.get(workerId: Long): Worker = worker(workerId)

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