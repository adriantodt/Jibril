package pw.aru.db.base

import pw.aru.db.AruDB
import pw.aru.utils.extensions.useResource
import java.util.*

abstract class RedisObject(val db: AruDB, val id: Long) {
    private val metadata = getOrCreateMetadata()

    val remoteExists: Boolean get() = db.pool.useResource { it.exists(remoteId()) }

    open fun delete() {
        db.pool.useResource { it.del(remoteId()) }
    }

    override fun toString() = metadata.toString(this)
    fun remoteId() = metadata.remoteId(this)
    fun remoteId(child: String) = metadata.remoteId(this, child)

    companion object {
        private val metadataContainer = WeakHashMap<Class<*>, RedisObjectMetadata>()

        @Suppress("NOTHING_TO_INLINE")
        private inline fun RedisObject.getOrCreateMetadata(): RedisObjectMetadata = metadataContainer.computeIfAbsent(javaClass, ::RedisObjectMetadata)
    }

    private class RedisObjectMetadata(c: Class<*>) {
        val javaClassName: String
        val javaClassSimpleName: String

        init {
            val fixedJavaClass: Class<*> = if (c.isAnonymousClass || c.isLocalClass) c.superclass else c
            javaClassName = fixedJavaClass.name
            javaClassSimpleName = fixedJavaClass.simpleName
        }

        fun remoteId(obj: RedisObject) = "$javaClassName:${obj.id}"
        fun remoteId(obj: RedisObject, child: String) = "$javaClassName.$child:${obj.id}"
        fun toString(obj: RedisObject) = "$javaClassSimpleName[id=${obj.id}]"
    }
}
