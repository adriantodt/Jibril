package pw.aru.utils.ratelimiter

data class RateLimit internal constructor(val timestamp: Long, val triesLeft: Int, val cooldown: Long, val spamAttempts: Int) {
    val cooldownReset: Long get() = timestamp + cooldown
    override fun toString() = "RateLimit{triesLeft=$triesLeft, cooldown=$cooldown, spamAttempts=$spamAttempts}"
}