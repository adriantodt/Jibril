package pw.aru.database

import jibril.snowflake.entities.SnowflakeGenerator
import jibril.snowflake.local.LocalGeneratorBuilder
import pw.aru.integration.EventPublisher
import pw.aru.utils.extensions.get
import pw.aru.utils.extensions.useResource
import redis.clients.jedis.exceptions.JedisConnectionException

typealias Redis = redis.clients.jedis.Jedis
typealias RedisPool = redis.clients.jedis.JedisPool

object AruDatabase {
    //Pool
    val pool = RedisPool()

    val isConnected: Boolean
        get() = try {
            pool.useResource(Redis::info); true
        } catch (e: JedisConnectionException) {
            false
        }

    //Event Stuff
    val publisher: EventPublisher by lazy { EventPublisher(pool) }

    private val generator: SnowflakeGenerator = LocalGeneratorBuilder()
        .setEpoch(1517400000000L)
        .setDatacenterIdBits(2L)
        .setWorkerIdBits(2L)
        .setSequenceBits(12L)
        .build()

    val idWorker = generator[0, 1]

}
