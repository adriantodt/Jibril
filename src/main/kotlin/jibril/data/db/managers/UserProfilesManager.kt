package jibril.data.db.managers

import jibril.data.db.RedisPool
import jibril.data.db.entities.UserProfile
import jibril.data.db.managers.base.ObjectData
import jibril.data.db.managers.base.VersionedObjectManager

class UserProfilesManager(pool: RedisPool) : VersionedObjectManager<UserProfile>(pool, "userProfile") {

    override fun new(id: Long): UserProfile = UserProfile(id)

    override fun read(version: Int, id: Long, input: ByteArray): UserProfile {
        check(version == 0) {
            "invalid version v$version"
        }

        return input.fromBytes<MutableMap<String, Any>>()
            .apply { put("id", id) }
            .convert()
    }

    override fun write(value: UserProfile): ObjectData {
        return ObjectData(
            id = value.id,
            version = 0,
            value = value.convert<MutableMap<String, Any>>()
                .apply { remove("id") }
                .asBytes()
        )
    }

}

