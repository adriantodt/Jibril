package pw.aru.utils.api

import net.dv8tion.jda.bot.sharding.ShardManager
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import pw.aru.utils.extensions.jsonStringOf
import pw.aru.utils.extensions.newCall

interface DBotsPoster {
    fun postStats()

    object Dummy : DBotsPoster {
        override fun postStats() = Unit
    }

    class APIImpl(
        private val shardManager: ShardManager,
        private val httpClient: OkHttpClient,
        private val apiKey: String
    ) : DBotsPoster {
        private val apiRequest = "https://bots.discord.pw/api/bots/${shardManager.shardCache.first().selfUser.id}/stats"

        override fun postStats() {
            httpClient.newCall {
                url(apiRequest)
                header("Authorization", apiKey)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("serverCount" to shardManager.guildCache.size())))
            }.execute()
        }
    }
}