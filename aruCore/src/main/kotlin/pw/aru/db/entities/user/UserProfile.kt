package pw.aru.db.entities.user

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject
import pw.aru.db.base.annotations.R

@R("pw.aru:user:profile")
class UserProfile(db: AruDB, id: Long) : RedisObject(db, id) {
    var credits by RedisField.Long(0)
    var premiumCredits by RedisField.Long(0)
    var lvl by RedisField.Long(0)
    var xp by RedisField.Long(0)
    var rep by RedisField.Long(0)

    var description by RedisField.NullableString()
}

