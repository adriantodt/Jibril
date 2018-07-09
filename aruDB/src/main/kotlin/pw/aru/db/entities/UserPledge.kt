package pw.aru.db.entities

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject

class UserPledge(db: AruDB, id: Long) : RedisObject(db, id) {
    var enabled: Boolean by RedisField.Boolean(false)

    var description: String? by RedisField.NullableString()
}

