//package pw.aru.integration
//
//import pw.aru.Aru
//import pw.aru.db.RedisPool
//import pw.aru.utils.AruTaskExecutor.task
//import pw.aru.utils.extensions.jsonStringOf
//import pw.aru.utils.extensions.useResource
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.atomic.AtomicInteger
//
//class EventPublisher(val pool: RedisPool) {
//
//    init {
//        val counter = AtomicInteger()
//
//        task(1, TimeUnit.MINUTES) {
//            pool.useResource {
//                it.publish(
//                    "aru:message.heartbeat",
//                    jsonStringOf(
//                        "count" to counter.getAndIncrement(),
//                        "uptime" to Aru.rawUptime
//                    )
//                )
//            }
//        }
//    }
//
//    fun publishStats(shardTotal: Long, guilds: Long, users: Long, musicAmount: Long, queueSize: Long) {
//        pool.useResource {
//            it.publish(
//                "aru:message.stats",
//                jsonStringOf(
//                    "shardTotal" to shardTotal,
//                    "guilds" to guilds,
//                    "users" to users,
//                    "musicAmount" to musicAmount,
//                    "queueSize" to queueSize
//                )
//            )
//        }
//    }
//
//}