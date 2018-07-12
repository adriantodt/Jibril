package pw.aru.utils.api

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import net.dv8tion.jda.bot.sharding.ShardManager

interface DBLPoster {
    fun postStats()

    object Dummy : DBLPoster {
        override fun postStats() = Unit
    }

    class APIImpl(
        private val shardManager: ShardManager,
        private val api: DiscordBotsAPI
    ) : DBLPoster {
        override fun postStats() {
            api.postStats(
                shardManager.shards
                    .map { it.guildCache.size().toInt() }
                    .toIntArray()
            ).async()
        }
    }
}