package pw.aru._obsolete.v1.db.entities.user

import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.RedisField
import pw.aru._obsolete.v1.db.base.RedisObject
import pw.aru._obsolete.v1.db.base.annotations.R

@R("pw.aru:user:pledge")
class UserPledge(db: AruDB, id: Long) : RedisObject(db, id) {
    var enabled by RedisField.Boolean(false)
    var patronBot by RedisField.Boolean(false)
    var since by RedisField.NullableLong()
    var amount by RedisField.Int(0)
}