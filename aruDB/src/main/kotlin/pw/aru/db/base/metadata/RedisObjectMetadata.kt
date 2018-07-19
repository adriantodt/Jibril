package pw.aru.db.base.metadata

import java.util.*

class RedisObjectMetadata private constructor(c: Class<*>) {
    val javaClassName: String
    val javaClassSimpleName: String

    init {
        val fixedJavaClass: Class<*> = if (c.isAnonymousClass || c.isLocalClass) c.superclass else c
        javaClassName = fixedJavaClass.name
        javaClassSimpleName = fixedJavaClass.simpleName
    }

    fun remoteId(id: Long) = "$javaClassName:$id"
    fun remoteId(id: Long, child: String) = "$javaClassName.$child:$id"
    fun toString(id: Long) = "$javaClassSimpleName[id=$id]"

    companion object {
        operator fun invoke(c: Class<*>): RedisObjectMetadata = container.computeIfAbsent(c, ::RedisObjectMetadata)
        private val container = WeakHashMap<Class<*>, RedisObjectMetadata>()
    }
}