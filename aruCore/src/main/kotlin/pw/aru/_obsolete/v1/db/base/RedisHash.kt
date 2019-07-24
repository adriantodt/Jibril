package pw.aru._obsolete.v1.db.base

import pw.aru._obsolete.v1.db.AruDB

class RedisHash<K, V>(
    val db: AruDB,
    private val hash: String,
    private val keySerializer: Serializer<K>,
    private val valueSerializer: Serializer<V>
) : MutableMap<K, V> {
    override val size: Int get() = db.conn.sync().hlen(hash).toInt()

    override fun containsKey(key: K): Boolean = db.conn.sync().hexists(hash, keySerializer.serialize(key))

    override fun containsValue(value: V): Boolean =
        db.conn.sync().hvals(hash).contains(valueSerializer.serialize(value))

    override fun get(key: K): V? {
        return valueSerializer.unserialize(db.conn.sync().hget(hash, keySerializer.serialize(key)) ?: return null)
    }

    override fun isEmpty(): Boolean = (size == 0)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = db.conn.sync().hkeys(hash).mapTo(LinkedHashSet()) { k ->
            object : MutableMap.MutableEntry<K, V> {
                override val key: K get() = keySerializer.unserialize(k)
                override val value: V
                    get() = valueSerializer.unserialize(
                        db.conn.sync().hget(
                            hash,
                            keySerializer.serialize(key)
                        )
                    )
                override fun setValue(newValue: V): V = put(key, value)!!
            }
        }

    override val keys: MutableSet<K>
        get() = db.conn.sync().hkeys(hash).mapTo(
            LinkedHashSet(),
            keySerializer::unserialize
        )

    override val values: MutableCollection<V>
        get() = db.conn.sync().hvals(hash).mapTo(
            ArrayList(),
            valueSerializer::unserialize
        )

    override fun clear() {
        db.conn.sync().del(hash)
    }

    override fun put(key: K, value: V): V? {
        return valueSerializer.unserialize(db.conn.sync().let {
            val last = it.hget(hash, keySerializer.serialize(key))
            it.hset(hash, keySerializer.serialize(key), valueSerializer.serialize(value))
            last
        } ?: return null)
    }

    override fun putAll(from: Map<out K, V>) {
        db.conn.sync().let {
            from.forEach { k, v ->
                it.hset(hash, keySerializer.serialize(k), valueSerializer.serialize(v))
            }
        }
    }

    override fun remove(key: K): V? {
        return valueSerializer.unserialize(db.conn.sync().let {
            val last = it.hget(hash, keySerializer.serialize(key))
            it.hdel(hash, keySerializer.serialize(key))
            last
        } ?: return null)
    }

    override fun hashCode(): Int = entries.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Map<*, *>) return false
        if (other is RedisHash<*, *> && other.hash == hash) return true
        if (size != other.size) return false

        return entries == other.entries
    }

    override fun toString(): String = db.conn.sync().hgetall(hash).entries
        .joinToString(prefix = "{", separator = ", ", postfix = "}") { (k, v) -> "${keySerializer(k)}=${valueSerializer(v)}" }
}