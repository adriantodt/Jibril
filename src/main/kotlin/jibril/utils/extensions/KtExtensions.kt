@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import jibril.utils.J
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.*
import java.util.concurrent.Future
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

val random = Random()

inline fun <E> List<E>.random(): E = this[random.nextInt(this.size)]

inline fun <E> Array<E>.random(): E = this[random.nextInt(this.size)]

inline fun <E> randomOf(vararg objects: E): E = objects.random()

fun now() = System.currentTimeMillis() / 1000

/**
 * Resolves a Java Type to Kotlin.
 * Used by PersistentKtsEvaluator
 */
val Type.kotlinTypeName: String
    get() = when (this) {
        is ParameterizedType -> {
            "${rawType.kotlinTypeName}<${actualTypeArguments.joinToString { it.kotlinTypeName }}>"
        }
        is Class<*> -> {
            val typeParameters = J.typeParametersSize(this)

            if (typeParameters == 0) {
                this.kotlin.qualifiedName!!
            } else {
                "${this.kotlin.qualifiedName!!}<${(0 until typeParameters).joinToString { "Any" }}>"
            }
        }
        is WildcardType -> {
            "Any"
        }
        is GenericArrayType -> {
            "kotlin.Array<${genericComponentType.typeName}>"
        }
        else -> {
            kotlin.io.println("???: $javaClass")
            typeName
        }
    }