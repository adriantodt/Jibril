package jibril.database.entities

import jibril.database.base.RedisField
import jibril.database.base.RedisHash
import jibril.database.base.RedisObject
import jibril.database.base.Serializer.AsIs
import jibril.database.base.Serializer.ToLong

class GuildSettings(id: Long) : RedisObject(id) {
    var prefix: String? by RedisField.NullableString()
    val assignableRoles: MutableMap<String, Long> = RedisHash("guild:settings:assignableRoles:$id", AsIs, ToLong)

    override fun delete() {
        super.delete()
        assignableRoles.clear()
    }
}