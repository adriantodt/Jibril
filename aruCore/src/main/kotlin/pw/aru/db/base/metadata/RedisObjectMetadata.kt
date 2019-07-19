package pw.aru.db.base.metadata

import pw.aru.db.base.annotations.R
import java.util.*

class RedisObjectMetadata private constructor(c: Class<*>) {
    val identifier: String
    val javaClassName: String
    val javaClassSimpleName: String

    init {
        val fixedJavaClass: Class<*> = if (c.isAnonymousClass || c.isLocalClass) c.superclass else c
        identifier = fixedJavaClass.getAnnotation(R::class.java)?.value
            ?: throw IllegalStateException("$fixedJavaClass lacks @R annotation")
        javaClassName = fixedJavaClass.name
        javaClassSimpleName = fixedJavaClass.simpleName
    }

    fun remoteId(id: Long) = "$identifier:$id"
    fun remoteId(id: Long, child: String) = "$identifier.$child:$id"
    fun toString(id: Long) = "$javaClassSimpleName[id=$id]"

    fun parseRemoteID(remoteID: String): RemoteID {
        if (!remoteID.startsWith(identifier)) {
            throw IllegalArgumentException("Wrong Metadata; id $remoteID does not match $identifier pattern")
        }

        val base = remoteID.substring(identifier.length)
        val idDiv = base.lastIndexOf(':')

        if (idDiv < 0) {
            throw IllegalArgumentException("Wrong Metadata; id $remoteID does not match $identifier:[id] pattern")
        }

        val id = base.substring(idDiv).toLongOrNull()
            ?: throw IllegalArgumentException("Wrong Metadata; id $remoteID does not match $identifier:[id] pattern")

        val child = if (base.startsWith('.')) base.substring(1, idDiv) else null

        return RemoteID(
            identifier,
            child,
            id
        )
    }

    companion object {
        private val container = WeakHashMap<Class<*>, RedisObjectMetadata>()

        @JvmStatic
        @JvmName("getOrCreate")
        operator fun invoke(c: Class<*>): RedisObjectMetadata = container.computeIfAbsent(c, ::RedisObjectMetadata)
    }

    data class RemoteID(
        val identifier: String,
        val child: String?,
        val id: Long
    ) {
        val hasChild: Boolean = child != null

        override fun toString(): String {
            if (child != null) {
                return "$identifier.$child:$id"
            }
            return "$identifier:$id"
        }
    }
}