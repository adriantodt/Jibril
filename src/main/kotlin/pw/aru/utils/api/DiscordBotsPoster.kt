package pw.aru.utils.api

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import net.dv8tion.jda.bot.sharding.ShardManager

interface DiscordBotsPoster {
    fun postStats()

    object Dummy : DiscordBotsPoster {
        override fun postStats() = Unit
    }

    class APIImpl(
        private val shardManager: ShardManager,
        private val api: DiscordBotsAPI
    ) : DiscordBotsPoster {
        override fun postStats() {
            api.postStats(
                shardManager.shards
                    .map { it.guildCache.size().toInt() }
                    .toIntArray()
            ).async()
        }
    }
}