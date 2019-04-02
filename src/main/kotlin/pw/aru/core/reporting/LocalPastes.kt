package pw.aru.core.reporting

import pw.aru.utils.extensions.lang.replaceEach
import java.io.File
import java.util.*

object LocalPastes {
    lateinit var parentUrl: String

    fun paste(title: String, content: String, lang: String = "none"): String {
        val fileId = ErrorReportHandler.fileWorker.generate()
        File("pastes").mkdirs()

        File("pastes/$fileId.html").writeText(
            File("assets/aru/templates/pastes.html").readText().replaceEach(
                "{date}" to Date().toString(),
                "{title}" to title.replaceEach(
                    "&" to "&amp;",
                    "\"" to "&quot;",
                    "'" to "&apos;",
                    "<" to "&lt;",
                    ">" to "&gt;"
                ),
                "{lang}" to lang,
                "{content}" to content.replaceEach(
                    "&" to "&amp;",
                    "\"" to "&quot;",
                    "'" to "&apos;",
                    "<" to "&lt;",
                    ">" to "&gt;"
                )
            )
        )

        return "$parentUrl/$fileId.html"
    }
}