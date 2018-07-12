package pw.aru.db

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

    private val generator: SnowflakeGenerator = LocalGeneratorBuilder()
        .setEpoch(1517400000000L)
        .setDatacenterIdBits(2L)
        .setWorkerIdBits(2L)
        .setSequenceBits(12L)
        .build()

    val idWorker = generator[database, worker]
}
