package pw.aru.database.entities

import pw.aru.database.base.RedisField
import pw.aru.database.base.RedisHash
import pw.aru.database.base.RedisObject
import pw.aru.database.base.Serializer.AsIs
import pw.aru.database.base.Serializer.Companion.redisObject
import pw.aru.database.base.Serializer.ToLong

class GuildSettings(id: Long) : RedisObject(id) {
    var prefix: String? by RedisField.NullableString()

    val assignableRoles: MutableMap<String, Long> = RedisHash(remoteId("assignableRoles"), AsIs, ToLong)
    val customCommands: MutableMap<String, CustomCommand> = RedisHash(remoteId("customCommands"), AsIs, redisObject(::CustomCommand))

    override fun delete() {
        super.delete()
        assignableRoles.clear()
        customCommands.clear()
    }
}