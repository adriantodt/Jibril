package jibril.data.db.managers.base

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.primitives.Bytes
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator.Feature.ENABLE_STREAMING
import jibril.data.db.RedisPool
import jibril.utils.extensions.binary

abstract class VersionedObjectManager<T>(
    private val pool: RedisPool, key: String
) : Iterable<T> {
    private val binaryKey = key.binary

    val all: List<T>
        get() = pool.resource
            .use { it.hgetAll(binaryKey) }
            .map { (k, v) -> rawGet(Longs.fromByteArray(k), v) }

    val keys: List<Long>
        get() = pool.resource
            .use { it.hkeys(binaryKey) }
            .map(Longs::fromByteArray)

    override fun iterator(): Iterator<T> = all.iterator()

    operator fun get(id: Long): T {
        return rawGet(id, pool.resource.use { it.hget(binaryKey, id.binary) } ?: return new(id))
    }

    fun getOrNull(id: Long): T? {
        return rawGet(id, pool.resource.use { it.hget(binaryKey, id.binary) } ?: return null)
    }

    private fun rawGet(id: Long, input: ByteArray): T {
        check(input.size >= 4) {
            "input too small; minimum 4 bytes"
        }

        val version = Ints.fromBytes(input[0], input[1], input[2], input[3])

        return read(version, id, input.copyOfRange(4, input.size))
    }

    fun save(value: T) {
        val (id, version, input) = write(value)
        pool.resource.use { it.hset(binaryKey, id.binary, Bytes.concat(version.binary, input)) }
    }

    fun delete(value: T) {
        val (id) = write(value)
        pool.resource.use { it.hdel(binaryKey, id.binary) }
    }

    protected abstract fun new(id: Long): T
    protected abstract fun read(version: Int, id: Long, input: ByteArray): T
    protected abstract fun write(value: T): ObjectData

    protected inline fun <reified T : Any> Any.convert(): T = mapper.convertValue(this)
    protected inline fun <reified T : Any> ByteArray.fromBytes(): T = mapper.readValue(this)
    protected fun Any.asBytes(): ByteArray = mapper.writeValueAsBytes(this)

    companion object {
        val mapper = ObjectMapper(BsonFactory().enable(ENABLE_STREAMING)).apply {
            registerKotlinModule()
            disable(FAIL_ON_IGNORED_PROPERTIES)
        }
    }
}

data class ObjectData(val id: Long, val version: Int, val value: ByteArray)