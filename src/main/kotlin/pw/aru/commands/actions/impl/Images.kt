package pw.aru.commands.actions.impl

import java.io.InputStream

data class Image(val fileName: String, val inputStream: () -> InputStream)

interface ImageProvider {
    fun provide(): Image
}

