package jibril.integration

import jibril.data.db.RedisPool
import jibril.utils.extensions.useResource
import org.json.JSONObject

class EventPublisher(val pool: RedisPool) {

    fun publishStats(shardTotal: Long, guilds: Long, users: Long, musicAmount: Long, queueSize: Long) {
        pool.useResource { db ->
            db.publish(
                "jibril.stats",
                JSONObject(
                    mapOf(
                        "shardTotal" to shardTotal,
                        "guilds" to guilds,
                        "users" to users,
                        "musicAmount" to musicAmount,
                        "queueSize" to queueSize
                    )
                ).toString()
            )
        }
    }

}