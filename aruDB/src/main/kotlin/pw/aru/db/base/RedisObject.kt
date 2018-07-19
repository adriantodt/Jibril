package pw.aru.db.base

import pw.aru.db.AruDB
import pw.aru.db.base.metadata.RedisObjectMetadata
import pw.aru.snow64.Snow64
import pw.aru.utils.extensions.useResource
import java.util.*

abstract class RedisObject(val db: AruDB, val id: Long) {
    private val metadata = RedisObjectMetadata(javaClass)

    val remoteExists: Boolean get() = db.pool.useResource { it.exists(remoteId()) }

    open fun delete() {
        db.pool.useResource { it.del(remoteId()) }
    }

    override fun toString() = metadata.toString(id)
    fun remoteId() = metadata.remoteId(id)
    fun remoteId(child: String) = metadata.remoteId(id, child)

}

fun main(args: Array<String>) {
    println(Snow64.fromSnowflake(Random().nextLong()))
}