package pw.aru.database.base

import pw.aru.database.AruDatabase.pool
import pw.aru.utils.extensions.useResource

abstract class RedisObject(val id: Long) {
    private val fixedJavaClass = javaClass.let { if (it.isAnonymousClass || it.isLocalClass) it.superclass else it }

    private val javaClassName = fixedJavaClass.name
    private val javaClassSimpleName = fixedJavaClass.simpleName

    fun remoteId() = "$javaClassName:$id"
    fun remoteId(child: String) = "$javaClassName.$child:$id"

    val remoteExists: Boolean get() = pool.useResource { it.exists(remoteId()) }

    open fun delete() {
        pool.useResource { it.del(remoteId()) }
    }

    override fun toString() = "$javaClassSimpleName[id=$id]"
}