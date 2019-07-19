package pw.aru.core.permissions

import kotlin.reflect.full.findAnnotation

class PermissionSerializer {
    private val map = LinkedHashMap<String, Permission>()

    fun add(values: Array<out Permission>) = apply {
        for (value in values) serialize(value)
    }

    fun serialize(value: Permission): String {
        val prefix = (value::class.findAnnotation<P>()?.value
            ?: throw IllegalStateException("Permission is not annotated with ${P::class.java}"))
        val name = value.name.toLowerCase()

        val s = "$prefix:$name"
        map[s] = value
        return s
    }

    fun unserialize(s: String): Permission {
        return map[s] ?: throw IllegalStateException("No key named $s")
    }
}