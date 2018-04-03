package jibril.utils.caches

import jibril.Jibril
import jibril.exported.user_agent
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class URLCache(private var cacheDir: File, cacheSize: Int) {
    companion object {
        val DEFAULT_CACHE_DIR = File("cache")
        private val saved = ConcurrentHashMap<String, File>()
    }

    private val cache: FileCache = FileCache(cacheSize)

    init {
        if (cacheDir.isFile) cacheDir.delete()
        cacheDir.mkdirs()
    }

    constructor(cacheSize: Int) : this(DEFAULT_CACHE_DIR, cacheSize)

    fun getFile(url: String): File {
        //Test directly
        val file = saved.getOrPut(url) { File(cacheDir, url.replace('/', '_').replace(':', '_')) }
        if (file.exists()) return file

        //Download and Cache
        val bytes = Jibril.httpClient.newCall(
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

    fun getInput(url: String): InputStream {
        return cache.input(getFile(url))
    }
}
