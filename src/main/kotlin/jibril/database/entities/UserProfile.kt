package jibril.database.entities

import jibril.database.base.RedisField
import jibril.database.base.RedisObject

class UserProfile(id: Long) : RedisObject(id) {
    var money: Long by RedisField.Long()
    var xp: Long by RedisField.Long()
    var rep: Long by RedisField.Long()

    var description: String by RedisField.String("*Nothing set.*")
}

