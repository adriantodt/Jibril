package jibril.data.db.entities

import jibril.data.db.ManagedDatabase
import jibril.data.db.ManagedObject

data class Announcement(
    override val id: Long = ManagedDatabase.idWorker.generate(),
    val title: String,
    val content: String
) : ManagedObject