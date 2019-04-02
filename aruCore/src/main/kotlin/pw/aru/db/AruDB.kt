package pw.aru.db

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import pw.aru.db.base.RedisObject
import pw.aru.db.base.metadata.RedisObjectMetadata
import pw.aru.io.AruIO
import pw.aru.sides.AruSide
import pw.aru.snowflake.SnowflakeConfig
import pw.aru.snowflake.entities.SnowflakeWorker
import pw.aru.snowflake.local.LocalGenerator
import java.io.Closeable

class AruDB(val side: AruSide, val sideId: Long, val uri: String = DEFAULT_REDIS_URI) : Closeable {

    val client: RedisClient
    val conn: StatefulRedisConnection<String, String>

    private val snowflakeWorker: SnowflakeWorker
    private val io by lazy { AruIO(this) }

    init {
        this.snowflakeWorker = aruSnowflakes
            .getDatacenter(side.ordinal.toLong())
            .getWorker(sideId)

        this.client = RedisClient.create(uri)
        this.conn = client.connect()
    }

    fun nextID(): Long {
        return snowflakeWorker.generate()
    }

    fun io(): AruIO {
        return io
    }

    fun <T : RedisObject> exists(table: Class<T>, id: Long): Boolean {
        val meta = RedisObjectMetadata(table)
        val existCount = conn.sync().exists(meta.remoteId(id))

        return existCount > 0
    }

    fun <T : RedisObject> keys(table: Class<T>): List<Long> {
        val meta = RedisObjectMetadata(table)
        val keysMatched = conn.sync().keys(meta.identifier + ":*")

        return keysMatched.asSequence()
            .map(meta::parseRemoteID)
            .filter { !it.hasChild }
            .mapTo(ArrayList()) { it.id }
    }

    inline fun <reified T : RedisObject> exists(id: Long): Boolean {
        return exists(T::class.java, id)
    }

    inline fun <reified T : RedisObject> keys(): List<Long> {
        return keys(T::class.java)
    }

    override fun close() {
        client.shutdown()
    }

    companion object {
        val aruSnowflakes = LocalGenerator(SnowflakeConfig(1517400000000L))
    }
}