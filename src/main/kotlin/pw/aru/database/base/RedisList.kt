package pw.aru.database.base

import pw.aru.database.AruDatabase.pool
import pw.aru.database.Redis
import pw.aru.utils.extensions.useResource

class RedisList<E>(
    private val hash: String,
    private val serializer: Serializer<E>
) : MutableList<E>, RandomAccess {
    override val size: Int
        get() = pool.useResource { it.llen(hash) }.toInt()

    override fun isEmpty(): Boolean = size == 0

    override fun get(index: Int): E = serializer.unserialize(pool.useResource { it.lindex(hash, index.toLong()) })

    override fun set(index: Int, element: E): E {
        checkElementIndex(index, size)

        val serialized = serializer.serialize(element)

        val old = pool.useResource {
            val s = it.lindex(hash, index.toLong())
            it.lset(hash, index.toLong(), serialized)

            return@useResource s
        }

        return serializer(old)
    }

    private fun all(redis: Redis): MutableList<String> {
        return redis.lrange(hash, 0, -1)
    }

    private fun replace(redis: Redis, elements: Collection<String>) {
        redis.multi().also {
            it.del(hash)
            it.lpush(hash, *elements.toTypedArray())
        }.exec()
    }

    override fun add(element: E): Boolean {
        val serialized = serializer.serialize(element)

        pool.useResource { it.lpush(hash, serialized) }

        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val serialized = elements.map(serializer::serialize).toTypedArray()

        pool.useResource { it.lpush(hash, *serialized) }

        return true
    }

    override fun add(index: Int, element: E) {
        checkElementIndex(index, size)

        if (index == size) add(element)

        pool.useResource {
            val all = all(it)
            all.add(index, serializer.serialize(element))
            replace(it, all)
        }
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        checkElementIndex(index, size)

        if (index == size) addAll(elements)

        pool.useResource {
            val all = all(it)
            all.addAll(index, elements.map(serializer::serialize))
            replace(it, all)
        }

        return true
    }

    override fun clear() {
        pool.useResource { it.del(hash) }
    }

    override fun remove(element: E): Boolean {
        pool.useResource {
            val all = all(it)
            val result = all.remove(serializer.serialize(element))
            replace(it, all)
            return result
        }
    }

    override fun removeAt(index: Int): E {
        checkElementIndex(index, size)

        val at = pool.useResource {
            val all = all(it)
            val at = all.removeAt(index)
            replace(it, all)
            return@useResource at
        }

        return serializer.unserialize(at)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        pool.useResource {
            val all = all(it)
            val result = all.removeAll(elements.map(serializer::serialize))
            replace(it, all)
            return result
        }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        pool.useResource {
            val all = all(it)
            val result = all.retainAll(elements.map(serializer::serialize))
            replace(it, all)
            return result
        }
    }

    override fun contains(element: E): Boolean = pool.useResource(this::all).contains(serializer.serialize(element))

    override fun containsAll(elements: Collection<E>): Boolean = pool.useResource(this::all).containsAll(elements.map(serializer::serialize))

    override fun indexOf(element: E): Int = pool.useResource(this::all).indexOf(serializer.serialize(element))

    override fun lastIndexOf(element: E): Int = pool.useResource(this::all).lastIndexOf(serializer.serialize(element))

    override fun iterator(): MutableIterator<E> = IteratorImpl()

    override fun listIterator(): MutableListIterator<E> = ListIteratorImpl(0)

    override fun listIterator(index: Int): MutableListIterator<E> = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = SubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false

        return orderedEquals(this, other)
    }

    override fun hashCode(): Int = orderedHashCode(this)

    override fun toString(): String = pool.useResource { it.lrange(hash, 0, -1) }
        .joinToString(prefix = "[", separator = ", ", postfix = "]") { serializer(it).toString() }

    private class SubList<E>(private val list: MutableList<E>, private val fromIndex: Int, toIndex: Int) : AbstractMutableList<E>(), RandomAccess {
        private var _size: Int = 0

        init {
            checkRangeIndexes(fromIndex, toIndex, list.size)
            this._size = toIndex - fromIndex
        }

        override fun get(index: Int): E {
            checkElementIndex(index, _size)

            return list[fromIndex + index]
        }

        override fun add(index: Int, element: E) {
            checkElementIndex(index, _size)

            list.add(fromIndex + index, element)
        }

        override fun removeAt(index: Int): E {
            checkElementIndex(index, _size)

            return list.removeAt(fromIndex + index)
        }

        override fun set(index: Int, element: E): E {
            checkElementIndex(index, _size)

            return list.set(fromIndex + index, element)
        }

        override val size: Int get() = _size
    }

    private open inner class IteratorImpl : MutableIterator<E> {
        protected var index = 0

        override fun hasNext(): Boolean = index < size

        override fun next(): E {
            if (!hasNext()) throw NoSuchElementException()
            return get(index++)
        }

        override fun remove() {
            removeAt(index)
        }
    }

    private open inner class ListIteratorImpl(index: Int) : IteratorImpl(), MutableListIterator<E> {

        init {
            checkPositionIndex(index, this@RedisList.size)
            this.index = index
        }

        override fun add(element: E) {
            add(index, element)
        }

        override fun set(element: E) {
            set(index, element)
        }

        override fun hasPrevious(): Boolean = index > 0

        override fun nextIndex(): Int = index

        override fun previous(): E {
            if (!hasPrevious()) throw NoSuchElementException()
            return get(--index)
        }

        override fun previousIndex(): Int = index - 1
    }

    internal companion object {
        internal fun checkElementIndex(index: Int, size: Int) {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        internal fun checkPositionIndex(index: Int, size: Int) {
            if (index < 0 || index > size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        internal fun checkRangeIndexes(fromIndex: Int, toIndex: Int, size: Int) {
            if (fromIndex < 0 || toIndex > size) {
                throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
            }
            if (fromIndex > toIndex) {
                throw IllegalArgumentException("fromIndex: $fromIndex > toIndex: $toIndex")
            }
        }

        internal fun orderedHashCode(c: Collection<*>): Int {
            var hashCode = 1
            for (e in c) {
                hashCode = 31 * hashCode + (e?.hashCode() ?: 0)
            }
            return hashCode
        }

        internal fun orderedEquals(c: Collection<*>, other: Collection<*>): Boolean {
            if (c.size != other.size) return false

            val otherIterator = other.iterator()
            for (elem in c) {
                val elemOther = otherIterator.next()
                if (elem != elemOther) {
                    return false
                }
            }
            return true
        }
    }
}