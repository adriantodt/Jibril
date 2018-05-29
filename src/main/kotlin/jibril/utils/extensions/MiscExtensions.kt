@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import jibril.snowflake.entities.SnowflakeDatacenter
import jibril.snowflake.entities.SnowflakeGenerator
import jibril.snowflake.entities.SnowflakeWorker
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Snowflakes
inline operator fun SnowflakeGenerator.get(datacenter: Long, worker: Long): SnowflakeWorker = getWorker(datacenter, worker)

inline operator fun SnowflakeGenerator.get(datacenter: Long): SnowflakeDatacenter = getDatacenter(datacenter)
inline operator fun SnowflakeDatacenter.get(worker: Long): SnowflakeWorker = getWorker(worker)

//Json
fun jsonOf(vararg pairs: Pair<*, *>): JSONObject = if (pairs.isNotEmpty()) JSONObject(mapOf(*pairs)) else JSONObject()

fun jsonStringOf(vararg pairs: Pair<*, *>) = jsonOf(*pairs).toString()

// OkHttp
inline fun OkHttpClient.newCall(builder: Request.Builder.() -> Unit): Call = newCall(Request.Builder().also(builder).build())

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