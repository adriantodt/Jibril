package pw.aru._obsolete.v1.db.entities.login

import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.RedisField
import pw.aru._obsolete.v1.db.base.RedisObject
import pw.aru._obsolete.v1.db.base.annotations.R

@R("pw.aru.dash:user:flat")
class FastLoginAuthToken(db: AruDB, id: Long) : RedisObject(db, id) {
    var userId by RedisField.Long()
}