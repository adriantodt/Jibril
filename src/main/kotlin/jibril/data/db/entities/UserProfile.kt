package jibril.data.db.entities

import jibril.data.db.ManagedObject

data class UserProfile(
    override val id: Long,
    var money: Long = 0
) : ManagedObject