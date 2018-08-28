@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import pw.aru.utils.Resource
import pw.aru.utils.SettableResource
import java.lang.reflect.Array.get
import java.lang.reflect.Array.getLength
import java.util.*
import kotlin.reflect.KProperty

// Json
fun jsonOf(vararg pairs: Pair<*, *>): JSONObject = if (pairs.isNotEmpty()) JSONObject(mapOf(*pairs)) else JSONObject()

fun jsonStringOf(vararg pairs: Pair<*, *>) = jsonOf(*pairs).toString()

// OkHttp
inline fun OkHttpClient.newCall(builder: Request.Builder.() -> Unit): Call = newCall(Request.Builder().also(builder).build())

// OkHttp JSON
inline fun ResponseBody.jsonObject() = JSONObject(string())

inline fun ResponseBody.jsonArray() = JSONArray(string())

// Resource
operator fun <T> Resource<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = resourceOrNull

operator fun <T> SettableResource<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    setResourceAvailable(value)
}

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

fun <E> List<E>.split(minSize: Int, maxSize: Int): List<List<E>> {
    return when {
        size < maxSize -> listOf(this)
        else -> chunked((minSize..maxSize).minBy { it - size % it } ?: (minSize + maxSize) / 2)
    }
}

fun Exception.simpleName(): String {
    var c: Class<*>? = javaClass

    while (c != null) {
        val name = c.simpleName
        if (!name.isEmpty()) return name
        c = c.superclass
    }

    return "Exception"
}

fun Exception.exceptionType(): String {
    var c: Class<*>? = javaClass

    while (c != null && c != Exception::class.java) {
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

    return "Exception"
}

/**
 * toString + array "unboxing" + builder reusing
 *
 * @param this@advancedToString Any object, from null to an array
 * @return if the object is an array, an actual array representation, else a toString()
 */
fun Any?.advancedToString(): String {
    return when {
        this == null -> "null"
        this.javaClass.isArray -> StringBuilder().advancedToString(this).toString()
        else -> toString()
    }
}

private fun StringBuilder.advancedToString(any: Any?) {
    when {
        any == null -> append("null")
        any.javaClass.isArray -> {
            val length = getLength(any)
            if (length == 0) {
                append("[]")
                return
            }
            for (i in 0 until length) append(if (i == 0) "[" else ", ").advancedToString(get(any, i))
            append("]")
        }
        else -> append(any)
    }
}

private val keys = listOf("*", "_", "`", "~~").map { it to Regex.fromLiteral(it) }
private val escapes = arrayOf("*" to "\\*", "_" to "\\_", "~" to "\\~")

private data class FormatToken(val format: String, val start: Int)

fun String.stripFormatting(): String {
    //all the formatting keys to keep track of

    //find all tokens (formatting strings described above)
    val tokens = keys.asSequence()
        .flatMap { (key, p) -> p.findAll(this).map { FormatToken(key, it.range.start) } }
        .sortedBy(FormatToken::start)

    //iterate over all tokens, find all matching pairs, and add them to the list toRemove
    val stack = Stack<FormatToken>()
    val toRemove = ArrayList<FormatToken>()
    var inBlock = false
    for (token in tokens) {
        if (stack.empty() || stack.peek().format != token.format || stack.peek().start + token.format.length == token.start) {
            //we are at opening tag
            if (!inBlock) {
                //we are outside of block -> handle normally
                if (token.format == "`") {
                    //block start... invalidate all previous tags
                    stack.clear()
                    inBlock = true
                }
                stack.push(token)
            } else if (token.format == "`") {
                //we are inside of a block -> handle only block tag
                stack.push(token)
            }
        } else if (!stack.empty()) {
            //we found a matching close-tag
            toRemove.add(stack.pop())
            toRemove.add(token)
            if (token.format == "`" && stack.empty()) {
                //close tag closed the block
                inBlock = false
            }
        }
    }

    //sort tags to remove by their start-index and iteratively build the remaining string
    toRemove.sortBy(FormatToken::start)

    val out = StringBuilder()
    var currIndex = 0
    for (formatToken in toRemove) {
        if (currIndex < formatToken.start) out.append(this, currIndex, formatToken.start)
        currIndex = formatToken.start + formatToken.format.length
    }
    if (currIndex < length) out.append(substring(currIndex))
    //return the stripped text, escape all remaining formatting characters (did not have matching open/close before or were left/right of block
    return out.toString().replaceEach(*escapes)
}
