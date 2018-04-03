package jibril.data.db.managers

import jibril.data.db.RedisPool
import jibril.data.db.entities.UserSettings
import jibril.data.db.managers.base.ObjectData
import jibril.data.db.managers.base.VersionedObjectManager

class UserSettingsManager(pool: RedisPool) : VersionedObjectManager<UserSettings>(pool, "userSettings") {

    override fun new(id: Long): UserSettings = UserSettings(id)

    override fun read(version: Int, id: Long, input: ByteArray): UserSettings {
        check(version == 0) {
            "invalid version v$version"
        }

        return input.fromBytes<MutableMap<String, Any>>()
            .apply { put("id", id) }
            .convert()
    }

    override fun write(value: UserSettings): ObjectData {
        return ObjectData(
            id = value.id,
            version = 0,
            value = value.convert<MutableMap<String, Any>>()
                .apply { remove("id") }
                .asBytes()
        )
    }

}