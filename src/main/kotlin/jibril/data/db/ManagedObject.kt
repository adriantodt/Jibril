package jibril.data.db

interface ManagedObject {
    val id: Long

    fun delete(db: ManagedDatabase) {
        db.delete(this)
    }

    fun save(db: ManagedDatabase) {
        db.save(this)
    }
}
