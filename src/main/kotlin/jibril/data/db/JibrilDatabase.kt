package jibril.data.db

import jibril.data.db.managers.AnnouncementsManager
import jibril.data.db.managers.GuildSettingsManager
import jibril.data.db.managers.UserProfilesManager
import jibril.data.db.managers.UserSettingsManager
import jibril.data.db.managers.base.VersionedObjectManager
import jibril.integration.EventPublisher
import jibril.snowflake.entities.SnowflakeGenerator
import jibril.snowflake.local.LocalGeneratorBuilder
import jibril.utils.extensions.get

typealias Redis = redis.clients.jedis.Jedis
typealias RedisPool = redis.clients.jedis.JedisPool

class JibrilDatabase(val pool: RedisPool) {

    constructor(uri: String) : this(RedisPool(uri))

    //Event Stuff
    val publisher: EventPublisher by lazy { EventPublisher(pool) }

    //Manager Manager
    private val managers = LinkedHashMap<Class<*>, VersionedObjectManager<out ManagedObject>>()

    //Managers
    val userSettings = UserSettingsManager(pool).register()
    val userProfiles = UserProfilesManager(pool).register()
    val guildSettings = GuildSettingsManager(pool).register()
    val announcements = AnnouncementsManager(pool).register()

    //Magic
    fun <T : ManagedObject> save(obj: T) {
        managerOf(obj).save(obj)
    }

    fun <T : ManagedObject> delete(obj: T) {
        managerOf(obj).delete(obj)
    }

    private fun <T : ManagedObject> managerOf(obj: T): VersionedObjectManager<T> {
        val manager = managers[obj.javaClass] ?: throw IllegalStateException("Unrecognized ManagedObject ${obj.javaClass.name}")
        @Suppress("UNCHECKED_CAST")
        return (manager as VersionedObjectManager<T>)
    }

    //Inline magic
    private inline fun <M : VersionedObjectManager<T>, reified T : ManagedObject> M.register(): M {
        managers[T::class.java] = this
        return this
    }

    companion object {
        private val generator: SnowflakeGenerator = LocalGeneratorBuilder()
            .setEpoch(1517400000000L)
            .setDatacenterIdBits(2L)
            .setWorkerIdBits(2L)
            .setSequenceBits(12L)
            .build()

        val idWorker = generator[0][1]
    }
}
