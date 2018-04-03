package jibril.data.db

import br.com.brjdevs.java.snowflakes.Snowflakes
import jibril.data.db.managers.AnnouncementsManager
import jibril.data.db.managers.GuildSettingsManager
import jibril.data.db.managers.UserProfilesManager
import jibril.data.db.managers.UserSettingsManager
import jibril.data.db.managers.base.VersionedObjectManager
import jibril.utils.extensions.get

typealias Redis = redis.clients.jedis.Jedis
typealias RedisPool = redis.clients.jedis.JedisPool

class ManagedDatabase(val pool: RedisPool) {
    companion object {
        private val factory = Snowflakes.config(1517400000000L, 2L, 2L, 12L)!!
        val idWorker = factory[0][1]
    }

    constructor(uri: String) : this(RedisPool(uri))

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
}
