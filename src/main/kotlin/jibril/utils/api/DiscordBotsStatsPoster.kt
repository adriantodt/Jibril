package jibril.utils.api

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import net.dv8tion.jda.bot.sharding.ShardManager
import javax.inject.Inject

interface DiscordBotsStatsPoster {
    fun postStats()
}

class DummyDBLStatsPoster : DiscordBotsStatsPoster {
    override fun postStats() {
    }
}

class DBLStatsPoster
@Inject constructor(
    private val shardManager: ShardManager,
    private val api: DiscordBotsAPI
) : DiscordBotsStatsPoster {
    override fun postStats() {
        api.postStats(shardManager.shards.map { it.guildCache.size().toInt() }.toIntArray())
    }
}