package pw.aru.db

import pw.aru.db.base.RedisObject
import pw.aru.db.base.metadata.RedisObjectMetadata
import pw.aru.snowflake.entities.SnowflakeGenerator
import pw.aru.snowflake.local.LocalGeneratorBuilder
import pw.aru.utils.extensions.get
import pw.aru.utils.extensions.useResource
import redis.clients.jedis.exceptions.JedisConnectionException

class AruDB(uri: String = "redis://localhost:6379", database: Long = 0, worker: Long = 0) {
    //Pool
    val pool = RedisPool(uri)

    val isConnected: Boolean
        get() = try {
            pool.useResource(Redis::info); true
        } catch (e: JedisConnectionException) {
            false
        }

    fun <T : RedisObject> exists(table: Class<T>, id: Long): Boolean = pool.useResource { it.exists(RedisObjectMetadata(table).remoteId(id)) }

    inline fun <reified T : RedisObject> exists(id: Long): Boolean = exists(T::class.java, id)

    val idWorker = generator[database, worker]

    companion object {
        val generator: SnowflakeGenerator = LocalGeneratorBuilder()
            .setEpoch(1517400000000L)
            .setDatacenterIdBits(2L)
            .setWorkerIdBits(2L)
            .setSequenceBits(12L)
            .build()
    }
}
