package pw.aru.commands.actions.v2

import java.io.InputStream

data class Image(val fileName: String, val inputStream: () -> InputStream)

interface ImageProvider {
    fun provide(): Image
}

