package jibril.data.db.managers

import jibril.data.db.RedisPool
import jibril.data.db.entities.GuildSettings
import jibril.data.db.managers.base.ObjectData
import jibril.data.db.managers.base.VersionedObjectManager

class AnnouncementsManager(pool: RedisPool) : VersionedObjectManager<GuildSettings>(pool, "announcements") {

    override fun new(id: Long): GuildSettings = GuildSettings(id)

    override fun read(version: Int, id: Long, input: ByteArray): GuildSettings {
        check(version == 0) {
            "invalid version v$version"
        }

        return input.fromBytes<MutableMap<String, Any>>()
            .apply { put("id", id) }
            .convert()
    }

    override fun write(value: GuildSettings): ObjectData {
        return ObjectData(
            id = value.id,
            version = 0,
            value = value.convert<MutableMap<String, Any>>()
                .apply { remove("id") }
                .asBytes()
        )
    }

}