package pw.aru.utils.extensions.lib

import org.json.JSONArray
import org.json.JSONObject

fun jsonOf(vararg pairs: Pair<*, *>) = if (pairs.isNotEmpty()) JSONObject(mapOf(*pairs)) else JSONObject()

fun jsonArrayOf(vararg contents: Any?) = JSONArray(contents)

fun jsonStringOf(vararg pairs: Pair<*, *>) = jsonOf(*pairs).toString()

fun jsonArrayStringOf(vararg contents: Any?) = jsonArrayOf(*contents).toString()

fun Map<*, *>.asJsonObject() = JSONObject(this)

fun Iterable<Pair<*, *>>.asJsonObject() = JSONObject(this.toMap())

fun Iterable<*>.asJsonArray() = JSONArray(this.toList())

fun Sequence<*>.asJsonArray() = JSONArray(this.toList())

