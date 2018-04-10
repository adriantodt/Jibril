package jibril.utils.api

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import net.dv8tion.jda.bot.sharding.ShardManager
import javax.inject.Inject

interface DBLPoster {
    fun postStats()

    class Dummy : DBLPoster {
        override fun postStats() = Unit
    }
}

class DiscordBotsAPIPoster
@Inject constructor(
    private val shardManager: ShardManager,
    private val api: DiscordBotsAPI
) : DBLPoster {
    override fun postStats() {
        api.postStats(shardManager.shards.map { it.guildCache.size().toInt() }.toIntArray()).async()
    }
}