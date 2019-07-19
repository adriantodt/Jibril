@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Extensions")
@file:JvmMultifileClass

package pw.aru.utils.extensions.lang

import java.lang.invoke.MethodHandles

fun Any?.advancedToString(): String {
    return when {
        this == null -> "null"
        this.javaClass.isArray -> StringBuilder().advancedToString(this).toString()
        else -> toString()
    }
}

fun Any?.toPrettyString(indentAmount: Int = 4, startingIndent: Int = 0): String {
    return StringBuilder().toPrettyString(this, indentAmount, startingIndent).toString()
}

private fun StringBuilder.advancedToString(any: Any?): StringBuilder {
    when {
        any == null -> append("null")
        any.javaClass.isArray -> {
            val length = java.lang.reflect.Array.getLength(any)
            if (length == 0) {
                append("[]")
                return this
            }
            for (i in 0 until length) append(if (i == 0) "[" else ", ").advancedToString(
                java.lang.reflect.Array.get(
                    any,
                    i
                )
            ).append("]")
        }
        else -> append(any)
    }

    return this
}

private fun StringBuilder.toPrettyString(
    any: Any?,
    indentAmount: Int = 2,
    currentIndent: Int = 0,
    indented: Boolean = false
): StringBuilder {
    val indent = List(currentIndent) { " " }.joinToString("")
    val firstIndent = if (indented) "" else indent
    when (any) {
        null -> {
            append(firstIndent).append("null")
        }
        is Array<*> -> {
            val length = java.lang.reflect.Array.getLength(any)
            if (length == 0) {
                append(firstIndent).append('[').append('\n').append(indent).append(']')
                return this
            }

            append("[\n")

            for (i in 0 until length) {
                toPrettyString(java.lang.reflect.Array.get(any, i), indentAmount, currentIndent + indentAmount)
                if (i + 1 != length) append(',')
                append("\n")
            }

            append(indent).append(']')
        }
        is Collection<*> -> {
            val size = any.size
            if (size == 0) {
                append(firstIndent).append('[').append('\n').append(indent).append(']')
                return this
            }

            append("[\n")

            val iterator = any.iterator()

            for (i in iterator) {
                toPrettyString(i, indentAmount, currentIndent + indentAmount)
                if (iterator.hasNext()) append(',')
                append("\n")
            }

            append(indent).append(']')
        }
        is Map<*, *> -> {
            val size = any.size
            if (size == 0) {
                append(firstIndent).append('{').append('\n').append(indent).append('}')
                return this
            }

            append("{\n")

            val iterator = any.iterator()

            for (i in iterator) {
                toPrettyString(i, indentAmount, currentIndent + indentAmount)
                if (iterator.hasNext()) append(',')
                append("\n")
            }

            append(indent).append('}')
        }
        is Map.Entry<*, *> -> {
            val (k, v) = any
            append(firstIndent).advancedToString(k).append(": ").toPrettyString(v, indentAmount, currentIndent, true)
        }
        else -> {
            append(firstIndent).append(any)
        }
    }

    return this
}


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

fun List<String>.limitedToString(limit: Int): String {
    if (isEmpty()) return "None"
    else {
        val builder = StringBuilder()
        val iterator = listIterator()

        while (iterator.hasNext()) {
            val next = iterator.next()

            if ((builder.length + next.length + 2) < limit) {
                builder.append(next)
                if (iterator.hasNext()) builder.append(", ")
            } else {
                builder.append("more ").append(size - iterator.nextIndex()).append("...")
                break
            }
        }

        return builder.toString()
    }
}

fun Any?.toStringReflexively(depth: Int = 0): String {
    return when {
        this == null -> this.toString()

        depth < 0 -> this.toString()

        else -> when (this) {
            is String, is Number -> this.toString()

            is Collection<*> -> this.map { it.toStringReflexively(depth - 1) }.toString()

            is Map<*, *> -> this.entries.asSequence().map { (k, v) ->
                k.toStringReflexively(depth - 1) to v.toStringReflexively(depth - 1)
            }.toMap().toString()

            is Array<*> -> this.map { it.toStringReflexively(depth - 1) }.toString()

            else -> {
                val lookup = MethodHandles.lookup()

                this.javaClass.name + generateSequence<Class<*>>(this.javaClass) { it.superclass }
                    .takeWhile { it != Object::class.java }
                    .flatMap { it.declaredFields.asSequence() }
                    .map {
                        it.name to lookup.unreflectGetter(it).invoke(this).toStringReflexively(depth - 1)
                    }.toMap().toString()
            }
        }
    }
}

