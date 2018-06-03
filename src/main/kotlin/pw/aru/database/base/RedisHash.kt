package pw.aru.database.base

import pw.aru.database.AruDatabase.pool
import pw.aru.utils.extensions.useResource

class RedisHash<K, V>(
    private val hash: String,
    private val keySerializer: Serializer<K>,
    private val valueSerializer: Serializer<V>
) : MutableMap<K, V> {
    override val size: Int get() = pool.useResource { it.hlen(hash).toInt() }

    override fun containsKey(key: K): Boolean = pool.useResource { it.hexists(hash, keySerializer.serialize(key)) }

    override fun containsValue(value: V): Boolean = pool.useResource { it.hvals(hash).contains(valueSerializer.serialize(value)) }

    override fun get(key: K): V? {
        return valueSerializer.unserialize(pool.useResource { it.hget(hash, keySerializer.serialize(key)) } ?: return null)
    }

    override fun isEmpty(): Boolean = (size == 0)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = pool.useResource { it.hkeys(hash) }.mapTo(LinkedHashSet()) { k ->
            object : MutableMap.MutableEntry<K, V> {
                override val key: K get() = keySerializer.unserialize(k)
                override val value: V get() = valueSerializer.unserialize(pool.useResource { it.hget(hash, keySerializer.serialize(key)) })
                override fun setValue(newValue: V): V = put(key, value)!!
            }
        }

    override val keys: MutableSet<K> get() = pool.useResource { it.hkeys(hash) }.mapTo(LinkedHashSet(), keySerializer::unserialize)

    override val values: MutableCollection<V> get() = pool.useResource { it.hvals(hash) }.mapTo(ArrayList(), valueSerializer::unserialize)

    override fun clear() {
        pool.useResource { it.del(hash) }
    }

    override fun put(key: K, value: V): V? {
        return valueSerializer.unserialize(pool.useResource {
            val last = it.hget(hash, keySerializer.serialize(key))
            it.hset(hash, keySerializer.serialize(key), valueSerializer.serialize(value))
            last
        } ?: return null)
    }

    override fun putAll(from: Map<out K, V>) {
        pool.useResource {
            from.forEach { k, v ->
                it.hset(hash, keySerializer.serialize(k), valueSerializer.serialize(v))
            }
        }
    }

    override fun remove(key: K): V? {
        return valueSerializer.unserialize(pool.useResource {
            val last = it.hget(hash, keySerializer.serialize(key))
            it.hdel(hash, keySerializer.serialize(key))
            last
        } ?: return null)
    }

    override fun hashCode(): Int = pool.useResource { it.hgetAll(hash) }.entries.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Map<*, *>) return false
        if (other is RedisHash<*, *> && other.hash == hash) return true
        if (size != other.size) return false

        return entries == other.entries
    }

    override fun toString(): String = pool.useResource { it.hgetAll(hash) }.entries
        .joinToString(prefix = "{", separator = ", ", postfix = "}") { (k, v) -> "${keySerializer(k)}=${valueSerializer(v)}" }
}