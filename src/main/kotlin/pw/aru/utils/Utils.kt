package pw.aru.utils

import pw.aru.core.reporting.ErrorReportHandler.Companion.fileWorker
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString
import java.io.File
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

fun paste(title: String, content: String, lang: String = "none"): String {
    val fileId = fileWorker.generate()
    File("pastes").mkdirs()

    File("pastes/$fileId.html").writeText(
        File("assets/aru/templates/pastes.html").readText().replaceEach(
            "{date}" to Date().toString(),
            "{title}" to title.replaceEach("&" to "&amp;", "\"" to "&quot;", "'" to "&apos;", "<" to "&lt;", ">" to "&gt;"),
            "{lang}" to lang,
            "{content}" to content.replaceEach("&" to "&amp;", "\"" to "&quot;", "'" to "&apos;", "<" to "&lt;", ">" to "&gt;")
        )
    )

    return "https://pastes.aru.pw/$fileId.html"
}

