@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("AruDB_ExtensionsKt")

package pw.aru.utils.extensions

import pw.aru.snowflake.entities.SnowflakeDatacenter
import pw.aru.snowflake.entities.SnowflakeGenerator
import pw.aru.snowflake.entities.SnowflakeWorker
import redis.clients.util.Pool
import java.io.Closeable

// Snowflakes
inline operator fun SnowflakeGenerator.get(datacenter: Long, worker: Long): SnowflakeWorker = getWorker(datacenter, worker)

inline operator fun SnowflakeGenerator.get(datacenter: Long): SnowflakeDatacenter = getDatacenter(datacenter)
inline operator fun SnowflakeDatacenter.get(worker: Long): SnowflakeWorker = getWorker(worker)

//Redis

inline fun <T : Closeable?, R> Pool<T>.useResource(block: (T) -> R) = resource.use(block)
