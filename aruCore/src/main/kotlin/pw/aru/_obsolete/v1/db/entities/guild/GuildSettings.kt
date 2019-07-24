package pw.aru._obsolete.v1.db.entities.guild

import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.RedisField
import pw.aru._obsolete.v1.db.base.RedisHash
import pw.aru._obsolete.v1.db.base.RedisObject
import pw.aru._obsolete.v1.db.base.Serializer.AsIs
import pw.aru._obsolete.v1.db.base.Serializer.ToLong
import pw.aru._obsolete.v1.db.base.annotations.R

@R("pw.aru:guild:settings")
class GuildSettings(db: AruDB, id: Long) : RedisObject(db, id) {
    var mainPrefix by RedisField.NullableString()
    var devPrefix by RedisField.NullableString()
    var patreonPrefix by RedisField.NullableString()

    var legacyPremium by RedisField.Boolean(false)
    var premiumSince by RedisField.NullableLong()

    //begin guild configuration
    var showImageboardInfo by RedisField.Boolean(true)

    val assignableRoles: MutableMap<String, Long> = RedisHash(db, remoteId(), AsIs, ToLong)
    //val customCommands: MutableMap<String, CustomCommand> = RedisHash(db, remoteId(), AsIs, redisObject(db, ::CustomCommand))

    override fun delete() {
        super.delete()
        assignableRoles.clear()
        //customCommands.clear()
    }
}