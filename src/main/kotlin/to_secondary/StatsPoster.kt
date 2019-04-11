package to_secondary

import com.mewna.catnip.Catnip
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import pw.aru.core.reporting.ErrorReporter
import pw.aru.utils.extensions.lib.jsonStringOf
import pw.aru.utils.extensions.lib.newCall

class StatsPoster(
    private val httpClient: OkHttpClient,
    private val tokens: Tokens
) {
    private fun postStats(catnip: Catnip) {
        //TODO Send this stats poster to aru-secondary
        val botId = catnip.selfUser()!!.id()
        val guildCount = catnip.cache().guilds().size()

        val json = MediaType.parse("application/json")

        // DBL
        try {
            httpClient.newCall {
                url("https://discordbots.org/api/bots/$botId/stats")
                header("Authorization", tokens.dblToken)
                post(RequestBody.create(json, jsonStringOf("server_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DBL", e)
        }

        // DBots
        try {
            httpClient.newCall {
                url("https://discord.bots.gg/api/v1/bots/$botId/stats")
                header("Authorization", tokens.dpwToken)
                post(RequestBody.create(json, jsonStringOf("guildCount" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("Dbots.PW", e)
        }

        // Bots for Discord
        try {
            httpClient.newCall {
                url("https://botsfordiscord.com/api/bot/$botId")
                header("Authorization", tokens.bfdToken)
                post(RequestBody.create(json, jsonStringOf("count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("BotsForDiscord", e)
        }

        // Botlist.space
        try {
            httpClient.newCall {
                url("https://botlist.space/api/bots/$botId")
                header("Authorization", tokens.blsToken)
                post(RequestBody.create(json, jsonStringOf("server_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("Botlist.Space", e)
        }

        // Divine Discord Bots
        try {
            httpClient.newCall {
                url("https://divinediscordbots.com/bots/$botId/stats")
                header("Authorization", tokens.ddbToken)
                post(RequestBody.create(json, jsonStringOf("server_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DivineDiscordBots", e)
        }

        // DBL2
        try {
            httpClient.newCall {
                url("https://discordbotlist.com/api/bots/$botId/stats")
                header("Authorization", tokens.dbl2Token)
                post(RequestBody.create(json, jsonStringOf("guilds" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DBL.com", e)
        }

        //Bots on Discord
        try {
            httpClient.newCall {
                url("https://bots.ondiscord.xyz/bot-api/bots/$botId/guilds")
                header("Authorization", tokens.bodToken)
                post(RequestBody.create(json, jsonStringOf("guildCount" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("BotsOnDiscord", e)
        }

        //Discord Bot World
        try {
            httpClient.newCall {
                url("https://discordbot.world/api/bot/$botId/stats")
                header("Authorization", tokens.dbwToken)
                post(RequestBody.create(json, jsonStringOf("guild_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordBotWorld", e)
        }

        //DiscordBotsGroup
        try {
            httpClient.newCall {
                url("https://discordbots.group/api/bot/$botId")
                header("Authorization", tokens.dbgToken)
                post(RequestBody.create(json, jsonStringOf("count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordBotsGroup", e)
        }

        //DiscordsBestBots
        try {
            httpClient.newCall {
                url("https://discordsbestbots.xyz/bots/$botId")
                header("Authorization", tokens.dbbToken)
                post(RequestBody.create(json, jsonStringOf("guilds" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("DiscordsBestBots", e)
        }

        //LBots
        try {
            httpClient.newCall {
                url("https://lbots.org/api/v1/bot/$botId/stats")
                header("Authorization", tokens.lboToken)
                post(RequestBody.create(json, jsonStringOf("guild_count" to guildCount)))
            }.execute().close()
        } catch (e: Exception) {
            handleStatsException("LBots", e)
        }

        //DiscordBoats
        try {
            httpClient.newCall {
                url("https://discord.boats/api/bot/$botId")
                header("Authorization", tokens.dboToken)
                post(RequestBody.create(json, jsonStringOf("server_count" to guildCount)))
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