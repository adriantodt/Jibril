package jibril.data.db.entities

import jibril.data.db.JibrilDatabase
import jibril.data.db.ManagedObject

data class Announcement(
    override val id: Long = JibrilDatabase.idWorker.generate(),
    val title: String,
    val content: String
) : ManagedObject