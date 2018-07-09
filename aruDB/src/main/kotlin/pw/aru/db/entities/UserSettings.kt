package pw.aru.db.entities

import pw.aru.db.AruDB
import pw.aru.db.base.RedisObject

class UserSettings(db: AruDB, id: Long) : RedisObject(db, id)