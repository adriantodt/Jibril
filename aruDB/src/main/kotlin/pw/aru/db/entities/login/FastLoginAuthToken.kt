package pw.aru.db.entities.login

import pw.aru.db.AruDB
import pw.aru.db.base.RedisField
import pw.aru.db.base.RedisObject

class FastLoginAuthToken(db: AruDB, id: Long) : RedisObject(db, id) {
    var userId by RedisField.Long()
}