package pw.aru.db.entities.user

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject
import pw.aru.db.base.annotations.R

@R("pw.aru:user:pledge")
class UserPledge(db: AruDB, id: Long) : RedisObject(db, id) {
    var enabled by RedisField.Boolean(false)
    var patronBot by RedisField.Boolean(false)
    var since by RedisField.NullableLong()
    var amount by RedisField.Int(0)
}