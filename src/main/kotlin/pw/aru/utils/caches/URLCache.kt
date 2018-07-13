package pw.aru.utils.caches

import okhttp3.OkHttpClient
import okhttp3.Request
import pw.aru.exported.user_agent
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
        val bytes = httpClient.newCall(
            Request.Builder()
                .url(url)
                .header("User-Agent", user_agent)
                .build()
        ).execute()
            .body()!!
            .bytes()

        file.writeBytes(bytes)

        return file
    }
}
