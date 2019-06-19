package pw.aru.utils.ratelimiter

import com.mewna.catnip.entity.user.User
import io.lettuce.core.RedisNoScriptException
import io.lettuce.core.ScriptOutputType
import pw.aru.core.patreon.Patreon
import pw.aru.db.AruDB
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

class RateLimiter(
    private val db: AruDB,
    private val prefix: String,
    private val limit: Int,
    private val cooldown: Int,
    private val spamBeforeCooldownIncrease: Int,
    private val cooldownIncrease: Int,
    private val maxCooldown: Int,
    private val randomIncrement: Boolean,
    private val premiumAware: Boolean
) {
    private var scriptSha: String? = null

    fun incrementAndGet(user: User): RateLimit {
        if (scriptSha == null) {
            scriptSha = db.conn.sync().scriptLoad(ratelimiterScript)
        }

        val start = Instant.now().toEpochMilli()
        val premiumAwareness = premiumAware && Patreon.isUserPremium(db, user)

        try {
            val cd = cooldown + if (randomIncrement) ThreadLocalRandom.current().nextInt(cooldown / 4) else 0

            val result = db.conn.sync().evalsha<List<Long>>(
                scriptSha,
                ScriptOutputType.MULTI,
                arrayOf("$prefix:${user.id()}"),
                limit.toString(),
                start.toString(),
                (if (premiumAwareness) cd - ThreadLocalRandom.current().nextInt(cooldown / 4) else cd).toString(),
                spamBeforeCooldownIncrease.toString(),
                cooldownIncrease.toString(),
                maxCooldown.toString()
            )

            return RateLimit(start, (limit - result[0]).toInt(), result[1] - start, result[2].toInt())
        } catch (e: RedisNoScriptException) {
            scriptSha = null
            return incrementAndGet(user)
        }
    }

    data class RateLimit(val timestamp: Long, val triesLeft: Int, val cooldown: Long, val spamAttempts: Int) {
        val success = triesLeft > 0
        val cooldownReset: Long get() = timestamp + cooldown
        override fun toString() = "RateLimit{triesLeft=$triesLeft, cooldown=$cooldown, spamAttempts=$spamAttempts}"
    }
}

private const val ratelimiterScript: String = """--[[
KEYS[1] - ratelimit key (redis hash)

ARGV[1] - ratelimit limit
ARGV[2] - current time
ARGV[3] - initial cooldown
ARGV[4] - maximum number of calls that can be done after limit is hit before cooldown is increased
ARGV[5] - increase in cooldown for each call after limit is hit
ARGV[6] - max cooldown time
]]

local data = redis.call("HGETALL", KEYS[1]) or {}

do
local tmp = {}
local k
for _,v in pairs(data) do
    if not k then
        k = v
    else
        tmp[k] = tonumber(v)
        k = nil
    end
end
data = tmp
end

local count = data.count or 0

local reset = data.reset or 0

local limit = tonumber(ARGV[1])
local now = tonumber(ARGV[2])
local initialCooldown = tonumber(ARGV[3])
local allowedSpam = tonumber(ARGV[4])
local incr = math.max(0, tonumber(ARGV[5]))
local maxCooldown = math.max(initialCooldown, tonumber(ARGV[6]))

if limit < 1 then
return redis.error_reply("Limit must be at least 1")
end

if initialCooldown < 1 then
return redis.error_reply("Initial cooldown must be at least 1")
end

if reset < now then
reset = now + initialCooldown
if count == limit then
    count = 0
end
end

if count >= limit then
local spam = data.spam or 0
if spam + 1 > allowedSpam then
    reset = math.min(reset + (incr * (spam - allowedSpam)), now + maxCooldown)
end

redis.call("HMSET", KEYS[1], "count", count, "reset", reset, "spam", spam + 1)
-- I'm giving you all two chances to back off before resetting your cooldown.
-- Use them wisely.
if spam + 1 > allowedSpam + 2 then
    redis.call("PEXPIRE", KEYS[1], reset)
end

return {count, reset, spam}
else
redis.call("HMSET", KEYS[1], "count", count + 1, "reset", reset, "spam", 0)
redis.call("PEXPIRE", KEYS[1], reset)

return {count, reset, 0}
end"""