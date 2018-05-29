package jibril.core.listeners

import jibril.data.config.Config
import jibril.utils.Colors
import jibril.utils.api.DiscordBotsPoster
import jibril.utils.extensions.*
import jibril.utils.helpers.GuildEvent
import jibril.utils.helpers.GuildStatsManager
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Region
import net.dv8tion.jda.core.entities.SelfUser
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.GenericGuildEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder

class GuildListener(
    private val shardManager: ShardManager,
    private val config: Config,
    private val botlistPoster: DiscordBotsPoster
) : EventListener {

    private fun log(user: SelfUser, embed: EmbedBuilder.() -> Unit) {
        WebhookClientBuilder(config.webhooks.serverLog!!).build().use {
            it.send(
                WebhookMessageBuilder()
                    .setUsername(user.name)
                    .setAvatarUrl(user.avatarUrl)
                    .addEmbeds(embed(init = embed))
                    .build()
            )
        }
    }

    override fun onEvent(event: Event) {
        if (event is GenericGuildEvent && (event is GuildJoinEvent || event is GuildLeaveEvent)) {
            log(event.jda.selfUser) {
                when (event) {
                    is GuildJoinEvent -> {
                        GuildStatsManager.log(GuildEvent.JOIN)
                        baseEmbed("JibrilLog | New Server", color = Colors.discordGreen)
                    }
                    is GuildLeaveEvent -> {
                        GuildStatsManager.log(GuildEvent.LEAVE)
                        baseEmbed("JibrilLog | Lost Server", color = Colors.discordRed)
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

            botlistPoster.postStats()
        }
    }

}