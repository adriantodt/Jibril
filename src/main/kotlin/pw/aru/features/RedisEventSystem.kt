//package pw.aru.features
//
//import mu.KLogging
//import org.json.JSONObject
//import pw.aru.db.AruDB
//import pw.aru.db.Redis
//import pw.aru.db.entities.user.UserPledge
//import kotlin.concurrent.thread
//
//typealias RedisPubSub = redis.clients.jedis.JedisPubSub
//
//class RedisEventSystem(val db: AruDB, val redis: Redis) {
//    companion object : KLogging()
//
//    init {
//        thread(name = "RedisEventSystem-reader", start = true) {
//            while (true) {
//                val (channel, message) = redis.blpop(
//                    "events.dbl.upvote",
//                    "events.patreon.create", "events.patreon.update", "events.patreon.delete"
//                )
//
//                onMessage(channel, message)
//            }
//        }
//    }
//
//    private fun onMessage(channel: String, message: String) {
//        when (channel) {
//            "events.dbl.upvote" -> processUpvote(JSONObject(message))
//            "events.patreon.create" -> createPatron(JSONObject(message))
//            "events.patreon.update" -> updatePatron(JSONObject(message))
//            "events.patreon.delete" -> deletePatron(JSONObject(message))
//            else -> {
//                logger.warn { "Channel not implemented: $channel" }
//            }
//        }
//    }
//
//    private fun processUpvote(message: JSONObject) {
//    }
//
//    private fun createPatron(message: JSONObject) {
//        val data = message["data"] as JSONObject
//        val database = message.getJSONArray("included").map {
//            val j = it as JSONObject
//            j.getString("id") to j
//        }.toMap()
//
//        val pledge = UserPledge(db, data.getString("id").toLong())
//        pledge.enabled = true
//
//    }
//
//    private fun updatePatron(message: JSONObject) {
//
//    }
//
//    private fun deletePatron(message: JSONObject) {
//
//    }
//}
