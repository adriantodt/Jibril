package pw.aru.core.hypervisor

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import pw.aru.core.config.AruConfig
import pw.aru.core.hypervisor.common.GuildWebhookLogger
import pw.aru.utils.extensions.jsonStringOf
import pw.aru.utils.extensions.newCall

class MainHypervisor(
    private val httpClient: OkHttpClient,
    private val config: AruConfig,
    private val dbl: DiscordBotsAPI
) : AruHypervisor {
    private val logger = GuildWebhookLogger(config.serversWebhook)
    override fun onBotStart(shardManager: ShardManager) {
        postStats(shardManager)
    }

    override fun onBotShutdown(shardManager: ShardManager) {
        postStats(shardManager)
    }

    override fun onGuildJoin(shardManager: ShardManager, guild: Guild) {
        logger.onGuildJoin(shardManager, guild)
        postStats(shardManager)
    }

    override fun onGuildLeave(shardManager: ShardManager, guild: Guild) {
        logger.onGuildLeave(shardManager, guild)
        postStats(shardManager)
    }

    private fun postStats(shardManager: ShardManager) {
        //DBL
        try {
            val stats = shardManager.shards.map { it.guildCache.size().toInt() }.toIntArray()
            dbl.postStats(stats).async()
        } catch (e: Exception) {
            //TODO Handle
        }

        //DBots
        try {
            val apiRequest = "https://bots.discord.pw/api/bots/${shardManager.shardCache.first().selfUser.id}/stats"
            httpClient.newCall {
                url(apiRequest)
                header("Authorization", config.dpwToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("serverCount" to shardManager.guildCache.size())))
            }.execute().close()
        } catch (e: Exception) {
            //TODO Handle
        }
    }
}