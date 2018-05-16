package jibril.database.base

import jibril.database.JibrilDatabase.pool
import jibril.utils.extensions.useResource

class RedisHash<K, V>(
    private val hash: String,
    private val keySerializer: Serializer<K>,
    private val valueSerializer: Serializer<V>
) : MutableMap<K, V> {
    override val size: Int get() = pool.useResource { it.hlen(hash).toInt() }

    override fun containsKey(key: K): Boolean = pool.useResource { it.hexists(hash, keySerializer.from(key)) }

    override fun containsValue(value: V): Boolean = pool.useResource { it.hvals(hash).contains(valueSerializer.from(value)) }

    override fun get(key: K): V? {
        return valueSerializer.to(pool.useResource { it.hget(hash, keySerializer.from(key)) } ?: return null)
    }

    override fun isEmpty(): Boolean = (size == 0)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = pool.useResource { it.hkeys(hash) }.mapTo(LinkedHashSet()) { k ->
            object : MutableMap.MutableEntry<K, V> {
                override val key: K get() = keySerializer.to(k)
                override val value: V get() = valueSerializer.to(pool.useResource { it.hget(hash, keySerializer.from(key)) })
                override fun setValue(newValue: V): V = put(key, value)!!
            }
        }

    override val keys: MutableSet<K> get() = pool.useResource { it.hkeys(hash) }.mapTo(LinkedHashSet(), keySerializer::to)

    override val values: MutableCollection<V> get() = pool.useResource { it.hvals(hash) }.mapTo(ArrayList(), valueSerializer::to)

    override fun clear() {
        pool.useResource { it.del(hash) }
    }

    override fun put(key: K, value: V): V? {
        return valueSerializer.to(pool.useResource {
            val last = it.hget(hash, keySerializer.from(key))
            it.hset(hash, keySerializer.from(key), valueSerializer.from(value))
            last
        } ?: return null)
    }

    override fun putAll(from: Map<out K, V>) {
        pool.useResource {
            from.forEach { k, v ->
                it.hset(hash, keySerializer.from(k), valueSerializer.from(v))
            }
        }
    }

    override fun remove(key: K): V? {
        return valueSerializer.to(pool.useResource {
            val last = it.hget(hash, keySerializer.from(key))
            it.hdel(hash, keySerializer.from(key))
            last
        } ?: return null)
    }

    override fun hashCode(): Int = pool.useResource { it.hgetAll(hash) }.entries.hashCode()

    override fun toString(): String = pool.useResource { it.hgetAll(hash) }.entries
        .joinToString(prefix = "{", separator = ", ", postfix = "}") { (k, v) -> "${keySerializer(k)}=${valueSerializer(v)}" }
}