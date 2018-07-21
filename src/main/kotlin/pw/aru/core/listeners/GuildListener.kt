package pw.aru.core.listeners

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Region
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.GenericGuildEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import pw.aru.core.config.AruConfig
import pw.aru.utils.Colors
import pw.aru.utils.api.DBLPoster
import pw.aru.utils.api.DBotsPoster
import pw.aru.utils.extensions.*
import pw.aru.utils.helpers.GuildEvent
import pw.aru.utils.helpers.GuildStatsManager

class GuildListener(
    private val shardManager: ShardManager,
    private val config: AruConfig,
    private val dbl: DBLPoster,
    private val dpw: DBotsPoster
) : EventListener {

    private fun log(embed: EmbedBuilder.() -> Unit) {
        WebhookClientBuilder(config.serversWebhook).build().use {
            it.send(WebhookMessageBuilder().addEmbeds(embed(init = embed)).build())
        }
    }

    override fun onEvent(event: Event) {
        if (event is GenericGuildEvent && (event is GuildJoinEvent || event is GuildLeaveEvent)) {
            log {
                when (event) {
                    is GuildJoinEvent -> {
                        GuildStatsManager.log(GuildEvent.JOIN)
                        baseEmbed("AruLog | New Server", color = Colors.discordGreen)
                    }
                    is GuildLeaveEvent -> {
                        GuildStatsManager.log(GuildEvent.LEAVE)
                        baseEmbed("AruLog | Lost Server", color = Colors.discordRed)
                    }
                }

                val guild = event.guild

                thumbnail(guild.iconUrl)

                val region = guild.region.let {
                    if (it == Region.UNKNOWN) guild.regionRaw + "\\*" else it.getName()
                }

                description(
                    "\u25AB **Name**: ${guild.name}",
                    "**M/TC/VC**: ${guild.memberCache.size()}/${guild.textChannelCache.size()}/${guild.voiceChannelCache.size()}",
                    "\u25AB **Region**: $region",
                    "\u25AB **Owner**: ${guild.owner.user.discordTag}"
                )

                if (guild.features.isNotEmpty()) {
                    descriptionBuilder
                        .append("\n\u25AB **Features**: ")
                        .append(guild.features.joinToString("` `", "`", "`"))
                }

                footer("Count: ${shardManager.guildCache.size()} | ID: ${guild.id}", event.jda.selfUser.effectiveAvatarUrl)
            }

            dbl.postStats()
            dpw.postStats()
        }
    }

}