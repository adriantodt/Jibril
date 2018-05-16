package jibril.database.base

import jibril.database.JibrilDatabase.pool
import jibril.utils.extensions.useResource

abstract class RedisObject(val id: Long) {
    val remoteId = "$javaClassName:$id"
    val remoteExists get() = pool.useResource { it.exists(remoteId)!! }

    open fun delete() {
        pool.useResource { it.del(remoteId) }
    }

    override fun toString() = "${javaClass.simpleName}[id=$id]"
}

private val RedisObject.javaClassName
    get() = javaClass.let { if (it.isAnonymousClass || it.isLocalClass) it.superclass else it }.name