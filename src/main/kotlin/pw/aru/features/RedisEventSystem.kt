package pw.aru.features

import mu.KLogging
import pw.aru.database.Redis
import kotlin.concurrent.thread

typealias RedisPubSub = redis.clients.jedis.JedisPubSub

class RedisEventSystem(val redis: Redis) {
    companion object : KLogging()

    init {
        thread(name = "RedisEventSystem-reader", start = true) {
            while (true) {
                val (channel, message) = redis.blpop(
                    "events.dbl.upvote",
                    "events.patreon.create", "events.patreon.update", "events.patreon.delete"
                )

                onMessage(channel, message)
            }
        }
    }

    private fun onMessage(channel: String, message: String) {
        when (channel) {
            "events.dbl.upvote" -> processUpvote(message)
            "events.patreon.create" -> createPatron(message)
            "events.patreon.update" -> updatePatron(message)
            "events.patreon.delete" -> deletePatron(message)
            else -> {
                logger.warn { "Channel not implemented: $channel" }
            }
        }
    }

    private fun processUpvote(message: String) {

    }

    private fun createPatron(message: String) {

    }

    private fun updatePatron(message: String) {

    }

    private fun deletePatron(message: String) {

    }
}
