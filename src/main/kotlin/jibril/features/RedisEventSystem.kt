package jibril.features

import jibril.data.db.Redis
import mu.KLogging

typealias RedisPubSub = redis.clients.jedis.JedisPubSub

class RedisEventSystem(val redis: Redis) : RedisPubSub() {
    companion object : KLogging()

    constructor(uri: String) : this(Redis(uri))

    init {
        redis.subscribe(
            this,
            "events.dbl.upvote",
            "events.patreon.create", "events.patreon.update", "events.patreon.delete"
        )
    }

    override fun onMessage(channel: String, message: String) {
        when (channel) {
            "events.dbl.upvote" -> processUpvote(message)
            "events.patreon.create" -> createPatron(message)
            "events.patreon.update" -> updatePatron(message)
            "events.patreon.delete" -> deletePatron(message)
            else -> {

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
