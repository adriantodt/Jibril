package pw.aru.db.entities

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject

class UserProfile(db: AruDB, id: Long) : RedisObject(db, id) {
    var money: Long by RedisField.Long(0)
    var xp: Long by RedisField.Long(0)
    var rep: Long by RedisField.Long(0)

    var description: String? by RedisField.NullableString()
}

