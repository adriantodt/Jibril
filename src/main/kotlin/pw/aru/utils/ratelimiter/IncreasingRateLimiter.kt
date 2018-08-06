package pw.aru.utils.ratelimiter

import pw.aru.db.RedisPool
import redis.clients.jedis.exceptions.JedisNoScriptException
import java.time.Instant

class IncreasingRateLimiter(
    private val pool: RedisPool,

    prefix: String = "",

    limit: TimeAmount,
    cooldown: TimeAmount,

    private val spamTolerance: Int = 0,

    private val cooldownPenaltyIncrease: Int = 0,
    maxCooldown: TimeAmount = TimeAmount(0)
) {
    private val prefix = if (prefix.isEmpty()) prefix else "$prefix:"
    private val limit: Long
    private val cooldown: Long
    private val maxCooldown: Long

    init {
        val limitMillis = limit.toMillis()
        check(limitMillis >= 0) { "Limit must be at least one millise." }
        this.limit = limitMillis

        val cooldownMillis = cooldown.toMillis()
        check(cooldownMillis >= 0) { "Cooldown must be non-negative." }
        this.cooldown = cooldownMillis

        check(spamTolerance >= 0) { "Spam-tolerance must be non-negative." }

        this.maxCooldown = maxOf(this.cooldown, maxCooldown.toMillis())
    }

    private var script: String? = null

    fun limit(key: String) = limit0(prefix + key)

    private fun limit0(key: String): RateLimit {
        pool.resource.use {
            val scriptId = script ?: it.scriptLoad(ratelimiterScript).apply { script = this }
            val start = Instant.now().toEpochMilli()
            return try {
                @Suppress("UNCHECKED_CAST")
                val result = it.evalsha(scriptId,
                    listOf(key),
                    listOf(limit, start, cooldown, spamTolerance, cooldownPenaltyIncrease, maxCooldown).map(Any::toString)
                ) as List<Long>

                RateLimit(start, (limit - result[0]).toInt(), result[1] - start, result[2].toInt())
            } catch (e: JedisNoScriptException) {
                script = null
                limit0(key)
            }
        }
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