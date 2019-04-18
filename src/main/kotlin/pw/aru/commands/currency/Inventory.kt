package pw.aru.commands.currency

import org.json.JSONArray
import org.json.JSONObject
import pw.aru.utils.extensions.lib.jsonOf
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.withLock

class Inventory() {
    private val items = ArrayList<Item>()
    private val lock = ReentrantLock()

    constructor(jsonString: String) : this() {
        items += JSONArray(jsonString).map { Item.fromJson(it as JSONObject) }
    }

    fun toJson(): JSONArray {
        lock.withLock {
            return JSONArray(items.map { it.toJson() })
        }
    }

    fun addItem(item: Item): Boolean {
        lock.withLock {
            val iterator = items.listIterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (item.sameKind(next)) {
                    iterator.set(item.merge(next))
                    return false
                }
            }
            items.add(item)
            return true
        }
    }

    fun removeItem(item: Item): Boolean {
        lock.withLock {
            return items.remove(item)
        }
    }

    fun getItems(): List<Item> {
        lock.withLock {
            return ArrayList(items)
        }
    }


    fun compactation() {
        lock.withLock {

            val snapshot = LinkedList(items)
            items.clear()

            while (snapshot.isNotEmpty()) {
                var item = snapshot.removeFirst()
                val iterator = snapshot.iterator()

                while (iterator.hasNext()) {
                    val next = iterator.next()

                    if (item.sameKind(next)) {
                        item = item.merge(next)
                        iterator.remove()
                    }
                }

                items.add(item)
            }
        }
    }
}

data class Item(val type: ItemType, val amount: Int, val metadata: JSONObject = JSONObject()) {
    companion object {
        fun fromJson(json: JSONObject): Item {
            return Item(
                json.getEnum(ItemType::class.java, "type"),
                json.getInt("amount"),
                json.optJSONObject("metadata") ?: JSONObject()
            )
        }
    }

    fun toJson(): JSONObject {
        return jsonOf("type" to type, "amount" to amount, "metadata" to metadata)
    }

    fun sameKind(item: Item): Boolean {
        if (type != item.type) return false

        if (metadata.isEmpty && item.metadata.isEmpty) return true

        return type.sameKind(metadata, item.metadata)
    }

    fun merge(item: Item): Item {
        return Item(
            type = type,
            amount = amount + item.amount,
            metadata = type.merge(metadata, item.metadata)
        )
    }
}

enum class ItemType {
    ;

    open fun sameKind(metadata1: JSONObject, metadata2: JSONObject): Boolean {
        return metadata1.toMap() == metadata2.toMap()
    }

    open fun merge(metadata1: JSONObject, metadata2: JSONObject): JSONObject {
        return metadata1
    }
}
