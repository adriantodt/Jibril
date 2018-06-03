package pw.aru.database.entities

import pw.aru.database.base.RedisField
import pw.aru.database.base.RedisObject

class UserProfile(id: Long) : RedisObject(id) {
    var money: Long by RedisField.Long(0)
    var xp: Long by RedisField.Long(0)
    var rep: Long by RedisField.Long(0)

    var description: String? by RedisField.NullableString()
}

