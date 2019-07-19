package pw.aru.commands.actions.impl.providers

import pw.aru.commands.actions.impl.Image
import pw.aru.commands.actions.impl.ImageProvider
import pw.aru.utils.URLCache
import pw.aru.utils.extensions.lang.random
import java.io.File

class LocalFileProvider(private val files: List<String>) : ImageProvider {
    override fun provide(): Image {
        val file = File(files.random())
        return Image(file.name, file.toURI().toString(), file::inputStream)
    }
}

class RandomURLProvider(private val cache: URLCache, private val images: List<String>) : ImageProvider {
    override fun provide(): Image {
        val url = images.random()
        return Image(url.substring(url.lastIndexOf('/') + 1), url) {
            cache.cacheToFile(url).inputStream()
        }
    }
}