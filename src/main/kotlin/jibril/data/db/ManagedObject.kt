package jibril.data.db

interface ManagedObject {
    val id: Long

    fun delete(db: JibrilDatabase) {
        db.delete(this)
    }

    fun save(db: JibrilDatabase) {
        db.save(this)
    }
}
