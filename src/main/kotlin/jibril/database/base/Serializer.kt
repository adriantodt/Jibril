package jibril.database.base

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

interface Serializer<T> {
    fun from(obj: T): String
    fun to(obj: String): T

    operator fun invoke(obj: String) = to(obj)

    companion object {
        fun <T> of(from: (T) -> String, to: (String) -> T): Serializer<T> = object : Serializer<T> {
            override fun from(obj: T) = from(obj)
            override fun to(obj: String) = to(obj)
        }

        fun <T> jackson(mapper: ObjectMapper, type: TypeReference<T>) = object : Serializer<T> {
            override fun from(obj: T): String = mapper.writeValueAsString(obj)
            override fun to(obj: String): T = mapper.readValue(obj, type)
        }
    }

    object AsIs : Serializer<String> {
        override fun from(obj: String): String = obj
        override fun to(obj: String): String = obj
    }

    object ToInt : Serializer<Int> {
        override fun from(obj: Int): String = obj.toString()
        override fun to(obj: String): Int = obj.toInt()
    }

    object ToLong : Serializer<Long> {
        override fun from(obj: Long): String = obj.toString()
        override fun to(obj: String): Long = obj.toLong()
    }

    object ToDouble : Serializer<Double> {
        override fun from(obj: Double): String = obj.toString()
        override fun to(obj: String): Double = obj.toDouble()
    }

}
