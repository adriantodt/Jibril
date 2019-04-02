package pw.aru.db.entities.user

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject
import pw.aru.db.base.annotations.R

@R("pw.aru:user:settings")
class UserSettings(db: AruDB, id: Long) : RedisObject(db, id) {
    var blacklisted by RedisField.Boolean(false)
    var pledgeKey by RedisField.NullableLong()

    var legacyPremium by RedisField.Boolean(false)
    var premiumSince by RedisField.NullableLong()
    var premiumAmount by RedisField.Int(0)
}