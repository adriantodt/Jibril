package jibril.integration

import jibril.Jibril
import jibril.database.RedisPool
import jibril.utils.TaskManager.task
import jibril.utils.extensions.jsonStringOf
import jibril.utils.extensions.useResource
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class EventPublisher(val pool: RedisPool) {

    init {
        val counter = AtomicInteger()

        task(1, TimeUnit.MINUTES) {
            pool.useResource {
                it.publish(
                    "jibril:event.heartbeat",
                    jsonStringOf(
                        "count" to counter.getAndIncrement(),
                        "uptime" to Jibril.rawUptime
                    )
                )
            }
        }
    }

    fun publishStats(shardTotal: Long, guilds: Long, users: Long, musicAmount: Long, queueSize: Long) {
        pool.useResource {
            it.publish(
                "jibril:event.stats",
                jsonStringOf(
                    "shardTotal" to shardTotal,
                    "guilds" to guilds,
                    "users" to users,
                    "musicAmount" to musicAmount,
                    "queueSize" to queueSize
                )
            )
        }
    }

}