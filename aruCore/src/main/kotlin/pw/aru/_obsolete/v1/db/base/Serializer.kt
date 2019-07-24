package pw.aru._obsolete.v1.db.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import pw.aru._obsolete.v1.db.AruDB

interface Serializer<T> {
    fun serialize(obj: T): String
    fun unserialize(s: String): T

    operator fun invoke(obj: String) = unserialize(obj)

    fun nullable(nullStr: String = "_nil"): Serializer<T?> = Nullable(this, nullStr)

    class Nullable<T>(private val parent: Serializer<T>, private val nullStr: String = "_nil") : Serializer<T?> {
        override fun serialize(obj: T?) = obj?.let { parent.serialize(obj) } ?: nullStr

        override fun unserialize(s: String): T? = if (s == nullStr) null else parent.unserialize(s)

        override fun nullable(nullStr: String): Serializer<T?> = this
    }

    companion object {
        fun <T> of(from: (T) -> String, to: (String) -> T): Serializer<T> = object : Serializer<T> {
            override fun serialize(obj: T) = from(obj)
            override fun unserialize(s: String) = to(s)
        }

        inline fun <reified T : Any> jackson(mapper: ObjectMapper) = jackson(mapper, jacksonTypeRef<T>())

        fun <T : Any> jackson(mapper: ObjectMapper, type: TypeReference<T>) = object : Serializer<T> {
            override fun serialize(obj: T): String = mapper.writeValueAsString(obj)
            override fun unserialize(s: String): T = mapper.readValue(s, type)
        }

        fun <T : RedisObject> redisObject(db: AruDB, from: (AruDB, Long) -> T) = object : Serializer<T> {
            override fun serialize(obj: T): String = obj.id.toString()
            override fun unserialize(s: String): T = from(db, s.toLong())
        }

        inline fun <reified T : Enum<T>> enum() = enum(T::class.java)

        fun <T : Enum<T>> enum(c: Class<T>) = object : Serializer<T> {
            override fun serialize(obj: T): String = "&${obj.ordinal}"
            override fun unserialize(s: String): T {
                check(s.startsWith("&")) { "the String is not a Enum reference." }
                return c.enumConstants[s.substring(1).toInt()]
            }
        }
    }

    object AsIs : Serializer<String> {
        override fun serialize(obj: String): String = obj
        override fun unserialize(s: String): String = s
    }

    object ToInt : Serializer<Int> {
        override fun serialize(obj: Int): String = obj.toString()
        override fun unserialize(s: String): Int = s.toInt()
    }

    object ToLong : Serializer<Long> {
        override fun serialize(obj: Long): String = obj.toString()
        override fun unserialize(s: String): Long = s.toLong()
    }

    object ToDouble : Serializer<Double> {
        override fun serialize(obj: Double): String = obj.toString()
        override fun unserialize(s: String): Double = s.toDouble()
    }

    object ToBoolean : Serializer<Boolean> {
        override fun serialize(obj: Boolean): String = obj.toString()
        override fun unserialize(s: String): Boolean = s.toBoolean()
    }
}
