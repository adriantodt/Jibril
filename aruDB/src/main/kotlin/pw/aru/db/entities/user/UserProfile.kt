package pw.aru.db.entities.user

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject

class UserProfile(db: AruDB, id: Long) : RedisObject(db, id) {
    var money by RedisField.Long(0)
    var xp by RedisField.Long(0)
    var rep by RedisField.Long(0)

    var description by RedisField.NullableString()
}

