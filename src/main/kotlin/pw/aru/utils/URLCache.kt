package pw.aru.utils

import pw.aru.exported.user_agent
import pw.aru.utils.extensions.lang.send
import java.io.File
import java.net.URI.create
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.ConcurrentHashMap

class URLCache(private val httpClient: HttpClient, private var cacheDir: File) {
    private val cachedLinks = ConcurrentHashMap<String, File>()

    init {
        if (cacheDir.isFile) cacheDir.delete()
        cacheDir.mkdirs()
    }

    fun cacheToFile(url: String): File {
        //Test directly
        val file = cachedLinks.getOrPut(url) { File(cacheDir, url.replace('/', '_').replace(':', '_')) }
        if (file.exists()) return file

        //Download and Cache

        httpClient.runCatching {
            send(BodyHandlers.ofFile(file.toPath())) {
                uri(create(url))
                header("User-Agent", user_agent)
            }
        }.onFailure { throw RuntimeException("Error while caching $url", it) }

        return file
    }
}
