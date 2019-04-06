package pw.aru.db.entities.user

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject
import pw.aru.db.base.Serializer.Companion.redisObject
import pw.aru.db.base.annotations.R
import java.util.*

@R("pw.aru:user:profile")
class UserProfile(db: AruDB, id: Long) : RedisObject(db, id) {
    var money by RedisField.Long(0)
    var premiumMoney by RedisField.Long(0)
    var lvl by RedisField.Long(0)
    var xp by RedisField.Long(0)
    var rep by RedisField.Long(0)

    var bdayD by RedisField.Int(0)
    var bdayM by RedisField.Int(0)
    var bdayY by RedisField.Int(0)

    val bday: GregorianCalendar?
        get() {
            val d = bdayD
            val m = bdayM
            val y = bdayY

            return if (d != 0 && m != 0 && y != 0) GregorianCalendar(y, m, d)
            else null
        }

    val relationship by RedisField.Nullable(redisObject(db, ::UserRelationship).nullable())

    var description by RedisField.NullableString()
}

