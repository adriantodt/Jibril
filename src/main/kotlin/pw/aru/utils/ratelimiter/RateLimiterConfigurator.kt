package pw.aru.utils.ratelimiter

import pw.aru.db.AruDB
import java.util.concurrent.TimeUnit

class RateLimiterConfigurator {
    private var prefix = ""
    private var limit = -1
    private var cooldown = -1
    private var cooldownPenaltyIncrease: Int = 0
    private var spamTolerance: Int = 0
    private var maxCooldown: Int = 0
    private var randomIncrement = true
    private var premiumAware = false

    fun premiumAware(aware: Boolean): RateLimiterConfigurator {
        this.premiumAware = aware
        return this
    }

    fun randomIncrement(incr: Boolean): RateLimiterConfigurator {
        this.randomIncrement = incr
        return this
    }

    fun prefix(prefix: String?): RateLimiterConfigurator {
        if (prefix == null) {
            this.prefix = ""
        } else {
            this.prefix = "$prefix:"
        }
        return this
    }

    fun limit(limit: Int): RateLimiterConfigurator {
        this.limit = limit
        return this
    }

    fun cooldown(amount: Int, unit: TimeUnit): RateLimiterConfigurator {
        val inMillis = unit.toMillis(amount.toLong()).toInt()
        if (inMillis < 1) {
            throw IllegalArgumentException("Must be at least one millisecond!")
        }
        this.cooldown = inMillis
        return this
    }

    fun cooldownPenaltyIncrease(amount: Int, unit: TimeUnit): RateLimiterConfigurator {
        val inMillis = unit.toMillis(amount.toLong()).toInt()
        if (inMillis < 1) {
            throw IllegalArgumentException("Must be at least one millisecond!")
        }
        this.cooldownPenaltyIncrease = inMillis
        return this
    }

    fun spamTolerance(tolerance: Int): RateLimiterConfigurator {
        if (tolerance < 0) {
            throw IllegalArgumentException("Must be 0 or positive")
        }
        this.spamTolerance = tolerance
        return this
    }

    fun maxCooldown(amount: Int, unit: TimeUnit): RateLimiterConfigurator {
        val inMillis = unit.toMillis(amount.toLong()).toInt()
        if (inMillis < cooldown) {
            throw IllegalArgumentException("Must be greater than or equal to initial cooldown!")
        }
        this.maxCooldown = inMillis
        return this
    }

    fun build(db: AruDB): RateLimiter {
        if (limit < 0) {
            throw IllegalStateException("Limit must be set")
        }
        if (cooldown < 0) {
            throw IllegalStateException("Cooldown must be set")
        }

        return RateLimiter(
            db,
            prefix,
            limit,
            cooldown,
            spamTolerance,
            cooldownPenaltyIncrease,
            maxCooldown,
            randomIncrement,
            premiumAware
        )
    }
}
