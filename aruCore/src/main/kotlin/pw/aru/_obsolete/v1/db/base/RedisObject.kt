package pw.aru._obsolete.v1.db.base

import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.metadata.RedisObjectMetadata

abstract class RedisObject(val db: AruDB, val id: Long) {
    private val metadata = RedisObjectMetadata(javaClass)

    val remoteExists: Boolean get() = db.conn.sync().exists(remoteId()) > 0

    open fun delete() {
        db.conn.sync().del(remoteId())
    }

    override fun toString() = metadata.toString(id)
    fun remoteId() = metadata.remoteId(id)
    fun remoteId(child: String) = metadata.remoteId(id, child)
}