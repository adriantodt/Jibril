package pw.aru._obsolete.v1.db.entities.user

import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.RedisField
import pw.aru._obsolete.v1.db.base.RedisObject
import pw.aru._obsolete.v1.db.base.annotations.R

@R("pw.aru:user:settings")
class UserSettings(db: AruDB, id: Long) : RedisObject(db, id) {
    var blacklisted by RedisField.Boolean(false)
    var pledgeKey by RedisField.NullableLong()

    var legacyPremium by RedisField.Boolean(false)
    var premiumSince by RedisField.NullableLong()
    var premiumAmount by RedisField.Int(0)

    //begin user configurations
    var showImageboardInfo by RedisField.NullableBoolean()
}