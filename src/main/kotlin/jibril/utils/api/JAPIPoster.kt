package jibril.utils.api

import jibril.Jibril
import jibril.core.music.MusicManager
import jibril.data.config.ApiConfig
import jibril.exported.user_agent
import net.dv8tion.jda.bot.sharding.ShardManager
import okhttp3.FormBody
import okhttp3.Request
import javax.inject.Inject

interface JAPIPoster {
    fun postStats()

    class Dummy : JAPIPoster {
        override fun postStats() = Unit
    }
}

class JibrilAPIStatsPoster
@Inject constructor(
    private val shardManager: ShardManager,
    private val musicManager: MusicManager,
    private val api: ApiConfig
) : JAPIPoster {
    override fun postStats() {
        val shardTotal = shardManager.shardsTotal
        val guilds = shardManager.guildCache.size()
        val users = shardManager.userCache.size()
        val musicAmount = musicManager.musicPlayers.size()
        val queueSize = musicManager.musicPlayers.valueCollection()
            .map { it.queue.size }
            .sum()

        Jibril.httpClient.newCall(
            Request.Builder()
                .url("http://${api.hostname}:${api.port}/api/stats/post")
                .header("User-Agent", user_agent)
                .header("Authorization", api.token)
                .post(
                    FormBody.Builder()
                        .add("shardTotal", shardTotal.toString())
                        .add("guilds", guilds.toString())
                        .add("users", users.toString())
                        .add("musicAmount", musicAmount.toString())
                        .add("queueSize", queueSize.toString())
                        .build()
                )
                .build()
        ).execute()
    }
}