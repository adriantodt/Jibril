@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import jibril.snowflake.entities.SnowflakeDatacenter
import jibril.snowflake.entities.SnowflakeGenerator
import jibril.snowflake.entities.SnowflakeWorker
import redis.clients.util.Pool
import java.io.Closeable

// Snowflakes
inline operator fun SnowflakeGenerator.get(datacenter: Long, worker: Long): SnowflakeWorker = getWorker(datacenter, worker)

inline operator fun SnowflakeGenerator.get(datacenter: Long): SnowflakeDatacenter = getDatacenter(datacenter)
inline operator fun SnowflakeDatacenter.get(worker: Long): SnowflakeWorker = getWorker(worker)

//Redis

inline fun <T : Closeable?, R> Pool<T>.useResource(block: (T) -> R) = resource.use(block)
