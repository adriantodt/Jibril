package pw.aru.core.hypervisor.common

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Region
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import pw.aru.utils.Colors
import pw.aru.utils.extensions.*

class GuildWebhookLogger(private val webhook: String) {
    fun onGuildJoin(shardManager: ShardManager, guild: Guild) {
        sendEmbed {
            baseEmbed("AruLog | New Server", color = Colors.discordGreen)
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

            footer("Count: ${shardManager.guildCache.size()} | ID: ${guild.id}", guild.jda.selfUser.effectiveAvatarUrl)
        }
    }

    fun onGuildLeave(shardManager: ShardManager, guild: Guild) {
        sendEmbed {
            baseEmbed("AruLog | Lost Server", color = Colors.discordRed)
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

            footer("Count: ${shardManager.guildCache.size()} | ID: ${guild.id}", guild.jda.selfUser.effectiveAvatarUrl)
        }
    }

    private fun sendEmbed(embed: EmbedBuilder.() -> Unit) {
        WebhookClientBuilder(webhook).build().use {
            it.send(WebhookMessageBuilder().addEmbeds(pw.aru.utils.extensions.embed(init = embed)).build())
        }
    }
}