package pw.aru.core.hypervisor

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import pw.aru.core.config.AruConfig
import pw.aru.core.hypervisor.common.GuildWebhookLogger
import pw.aru.core.reporting.ErrorReporter
import pw.aru.utils.extensions.lib.jsonStringOf
import pw.aru.utils.extensions.lib.newCall

class MainHypervisor(
    private val httpClient: OkHttpClient,
    private val config: AruConfig
) : AruHypervisor {
    private val logger = GuildWebhookLogger(config.serversWebhook)
    override fun onBotStart(catnip: Catnip) {
        postStats(catnip)
    }

    override fun onBotShutdown(catnip: Catnip) {
        postStats(catnip)
    }

    override fun onGuildJoin(catnip: Catnip, guild: Guild) {
        logger.onGuildJoin(catnip, guild)
        postStats(catnip)
    }

    override fun onGuildLeave(catnip: Catnip, guild: Guild) {
        logger.onGuildLeave(catnip, guild)
        postStats(catnip)
    }

    private fun postStats(catnip: Catnip) {
        val botId = catnip.selfUser()!!.id()
        val guildCount = catnip.cache().guilds().size()

        // DBL
        try {
            httpClient.newCall {
                url("https://discordbots.org/api/bots/$botId/stats")
                header("Authorization", config.dblToken)
                post(
                    RequestBody.create(
                        MediaType.parse("application/json"),
                        jsonStringOf("server_count" to guildCount)
                    )
                )
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DBL", e)
        }

        // DBots
        try {
            httpClient.newCall {
                url("https://discord.bots.gg/api/v1/bots/$botId/stats")
                header("Authorization", config.dpwToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guildCount" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("Dbots.PW", e)
        }

        // Bots for Discord
        try {
            httpClient.newCall {
                url("https://botsfordiscord.com/api/bot/$botId")
                header("Authorization", config.bfdToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("BotsForDiscord", e)
        }

        // Botlist.space
        try {
            httpClient.newCall {
                url("https://botlist.space/api/bots/$botId")
                header("Authorization", config.blsToken)
                post(
                    RequestBody.create(
                        MediaType.parse("application/json"),
                        jsonStringOf("server_count" to guildCount)
                    )
                )
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("Botlist.Space", e)
        }

        // Divine Discord Bots
        try {
            httpClient.newCall {
                url("https://divinediscordbots.com/bots/$botId/stats")
                header("Authorization", config.ddbToken)
                post(
                    RequestBody.create(
                        MediaType.parse("application/json"),
                        jsonStringOf("server_count" to guildCount)
                    )
                )
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DivineDiscordBots", e)
        }

        // DBL2
        try {
            httpClient.newCall {
                url("https://discordbotlist.com/api/bots/$botId/stats")
                header("Authorization", config.dbl2Token)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guilds" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DBL.com", e)
        }

        //Bots on Discord
        try {
            httpClient.newCall {
                url("https://bots.ondiscord.xyz/bot-api/bots/$botId/guilds")
                header("Authorization", config.bodToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guildCount" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("BotsOnDiscord", e)
        }

        //Discord Bot World
        try {
            httpClient.newCall {
                url("https://discordbot.world/api/bot/$botId/stats")
                header("Authorization", config.dbwToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guild_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordBotWorld", e)
        }

        //DiscordBotsGroup
        try {
            httpClient.newCall {
                url("https://discordbots.group/api/bot/$botId")
                header("Authorization", config.dbgToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordBotsGroup", e)
        }

        //DiscordsBestBots
        try {
            httpClient.newCall {
                url("https://discordsbestbots.xyz/bots/$botId")
                header("Authorization", config.dbbToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guilds" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordsBestBots", e)
        }

        //LBots
        try {
            httpClient.newCall {
                url("https://lbots.org/api/v1/bot/$botId/stats")
                header("Authorization", config.lboToken)
                post(RequestBody.create(MediaType.parse("application/json"), jsonStringOf("guild_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("LBots", e)
        }

        //DiscordBoats
        try {
            httpClient.newCall {
                url("https://discord.boats/api/bot/$botId")
                header("Authorization", config.dboToken)
                post(
                    RequestBody.create(
                        MediaType.parse("application/json"),
                        jsonStringOf("server_count" to guildCount)
                    )
                )
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordBoats", e)
        }
    }

    private fun handleStatsException(botlistName: String, e: Exception) {
        ErrorReporter()
            .exception(e)
            .extra("botlist", botlistName)
            .report()
            .logToFile()
            .logAsError()
    }
}