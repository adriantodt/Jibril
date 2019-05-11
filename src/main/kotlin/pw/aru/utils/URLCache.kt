package pw.aru.utils

import okhttp3.OkHttpClient
import pw.aru.exported.user_agent
import pw.aru.utils.extensions.lib.body
import pw.aru.utils.extensions.lib.newCall
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class URLCache(private val httpClient: OkHttpClient, private var cacheDir: File) {
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
        httpClient.newCall {
            url(url)
            header("User-Agent", user_agent)
        }.execute().body { it.source().inputStream().copyTo(file.outputStream()) }

        return file
    }
}
