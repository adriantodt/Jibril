package jibril.utils

import jibril.Jibril.httpClient
import jibril.exported.user_agent
import jibril.utils.emotes.DISAPPOINTED
import jibril.utils.extensions.newCall
import jibril.utils.extensions.toSmartString
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.*

fun humanizedTime(millis: Long): String {
    val days = millis / 86400000
    val hours = millis / 3600000 % 24
    val minutes = millis / 60000 % 60
    val seconds = millis / 1000 % 60

    val parts = LinkedList<String>()

    if (days > 0) parts += "$days ${if (days == 1L) "day" else "days"}"
    if (hours > 0) parts += "$hours ${if (hours == 1L) "hour" else "hours"}"
    if (minutes > 0) parts += "$minutes ${if (minutes == 1L) "minute" else "minutes"}"
    if (seconds > 0) parts += "$seconds ${if (seconds == 1L) "second" else "seconds"}"

    return if (parts.isEmpty()) "0 seconds (about now...)" else parts.toSmartString()
}

fun String.limit(size: Int): String {
    return if (length <= size) this else substring(0, size - 3) + "..."
}

fun paste(contents: String): String {
    try {
        httpClient.newCall {
            url("https://hastebin.com/documents")

            header("User-Agent", user_agent)
            header("Content-Type", "text/plain")

            post(RequestBody.create(MediaType.parse("text/plain"), contents))
        }.execute().use {
            return "https://hastebin.com/${JSONObject(it.body()!!.string()).getString("key")}"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return "$DISAPPOINTED Hastebin is unavailable right now."
    }

}

