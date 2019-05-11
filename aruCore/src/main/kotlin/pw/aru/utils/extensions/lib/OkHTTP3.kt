package pw.aru.utils.extensions.lib

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import okhttp3.Response
import okhttp3.ResponseBody

inline fun OkHttpClient.newCall(block: Builder.() -> Unit): Call {
    return newCall(Builder().also(block).build())
}

inline fun <R> Call.execute(block: Response.() -> R): R {
    return execute().use(block)
}

inline fun <R> Response.body(block: (ResponseBody) -> R): R {
    return body()?.let(block) ?: throw IllegalStateException("Body is null")
}