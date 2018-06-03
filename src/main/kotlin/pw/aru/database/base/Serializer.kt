package pw.aru.database.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

interface Serializer<T> {
    fun serialize(obj: T): String
    fun unserialize(obj: String): T

    operator fun invoke(obj: String) = unserialize(obj)

    companion object {
        fun <T> of(from: (T) -> String, to: (String) -> T): Serializer<T> = object : Serializer<T> {
            override fun serialize(obj: T) = from(obj)
            override fun unserialize(obj: String) = to(obj)
        }

        fun <T> jackson(mapper: ObjectMapper, type: TypeReference<T>) = object : Serializer<T> {
            override fun serialize(obj: T): String = mapper.writeValueAsString(obj)
            override fun unserialize(obj: String): T = mapper.readValue(obj, type)
        }

        fun <T : RedisObject> redisObject(from: (Long) -> T) = object : Serializer<T> {
            override fun serialize(obj: T): String = obj.id.toString()
            override fun unserialize(obj: String): T = from(obj.toLong())
        }

        inline fun <reified T : Enum<T>> enum() = enum(T::class.java)

        fun <T : Enum<T>> enum(c: Class<T>) = object : Serializer<T> {
            override fun serialize(obj: T): String = "&${obj.ordinal}"
            override fun unserialize(obj: String): T {
                check(obj.startsWith("&")) { "the String is not a Enum reference." }
                return c.enumConstants[obj.substring(1).toInt()]
            }
        }
    }

    object AsIs : Serializer<String> {
        override fun serialize(obj: String): String = obj
        override fun unserialize(obj: String): String = obj
    }

    object ToInt : Serializer<Int> {
        override fun serialize(obj: Int): String = obj.toString()
        override fun unserialize(obj: String): Int = obj.toInt()
    }

    object ToLong : Serializer<Long> {
        override fun serialize(obj: Long): String = obj.toString()
        override fun unserialize(obj: String): Long = obj.toLong()
    }

    object ToDouble : Serializer<Double> {
        override fun serialize(obj: Double): String = obj.toString()
        override fun unserialize(obj: String): Double = obj.toDouble()
    }
}
