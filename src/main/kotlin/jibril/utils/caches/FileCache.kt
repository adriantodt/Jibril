package jibril.utils.caches

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.ExecutionException

class FileCache @JvmOverloads constructor(maxSize: Int, concurrencyLevel: Int = 10) {
    private val cache: LoadingCache<File, ByteArray> = CacheBuilder.newBuilder()
        .maximumSize(maxSize.toLong())
        .concurrencyLevel(concurrencyLevel)
        .build(CacheLoader.from<File, ByteArray> {
            if (it == null) throw IllegalStateException()
            if (!it.isFile) throw IllegalStateException(it.toString() + ": not a file")
            it.readBytes()
        })

    operator fun get(file: File): ByteArray {
        return get(file, true)
    }

    private fun get(file: File, copy: Boolean): ByteArray {
        try {
            return if (copy) cache.get(file).clone() else cache.get(file)
        } catch (e: ExecutionException) {
            throw RuntimeException(e.cause)
        }
    }

    fun input(file: File): InputStream {
        return ByteArrayInputStream(get(file, false))
    }
}
