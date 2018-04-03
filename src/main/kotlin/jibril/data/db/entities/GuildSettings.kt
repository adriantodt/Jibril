package jibril.data.db.entities

import jibril.data.db.ManagedObject

data class GuildSettings(
    override val id: Long,
    var prefix: String? = null,

    //Assignable Roles
    val assignableRoles: MutableMap<String, Long> = LinkedHashMap()

) : ManagedObject