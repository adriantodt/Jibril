package pw.aru.db.entities.guild

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisHash
import pw.aru.db.base.RedisObject
import pw.aru.db.base.Serializer.AsIs
import pw.aru.db.base.Serializer.ToLong
import pw.aru.db.base.annotations.R

@R("pw.aru:guild:settings")
class GuildSettings(db: AruDB, id: Long) : RedisObject(db, id) {
    var prefix by RedisField.NullableString()

    val assignableRoles: MutableMap<String, Long> = RedisHash(db, remoteId(), AsIs, ToLong)
    //val customCommands: MutableMap<String, CustomCommand> = RedisHash(db, remoteId(), AsIs, redisObject(db, ::CustomCommand))

    override fun delete() {
        super.delete()
        assignableRoles.clear()
        //customCommands.clear()
    }
}