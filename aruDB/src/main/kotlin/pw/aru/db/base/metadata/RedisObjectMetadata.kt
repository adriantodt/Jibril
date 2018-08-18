package pw.aru.db.base.metadata

import pw.aru.db.base.annotations.R
import java.util.*

class RedisObjectMetadata private constructor(c: Class<*>) {
    val identifier: String
    val javaClassName: String
    val javaClassSimpleName: String

    init {
        val fixedJavaClass: Class<*> = if (c.isAnonymousClass || c.isLocalClass) c.superclass else c
        identifier = fixedJavaClass.getAnnotation(R::class.java)?.value ?: throw IllegalStateException("$fixedJavaClass lacks @R annotation")
        javaClassName = fixedJavaClass.name
        javaClassSimpleName = fixedJavaClass.simpleName
    }

    fun remoteId(id: Long) = "$identifier:$id"
    fun remoteId(id: Long, child: String) = "$identifier.$child:$id"
    fun toString(id: Long) = "$javaClassSimpleName[id=$id]"

    companion object {
        private val container = WeakHashMap<Class<*>, RedisObjectMetadata>()

        @JvmStatic
        @JvmName("getOrCreate")
        operator fun invoke(c: Class<*>): RedisObjectMetadata = container.computeIfAbsent(c, ::RedisObjectMetadata)
    }
}