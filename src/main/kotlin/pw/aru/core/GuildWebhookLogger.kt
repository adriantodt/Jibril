package pw.aru.core

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.channel.GuildChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.message.MessageOptions
import com.mewna.catnip.util.Utils.parseWebhook
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.extensions.lang.plusAssign
import pw.aru.utils.extensions.lib.embed

class GuildWebhookLogger(private val webhook: String) {
    fun onGuildJoin(guild: Guild) {
        sendEmbed(guild.catnip()) {
            author("AruLog | New Server")
            color(Colors.discordGreen)
            thumbnail(guild.iconUrl())

            val builder = StringBuilder()

            builder += multiline(
                "\u25AB **Name**: ${guild.name()}",
                "**M/TC/VC**: ${guild.memberCount()}/${guild.channels().count(GuildChannel::isText)}/${guild.channels().count(
                    GuildChannel::isVoice
                )}",
                "\u25AB **Region**: ${guild.region()}",
                "\u25AB **Owner**: ${guild.owner().user().discordTag()}"
            )

            if (guild.features().isNotEmpty()) {
                builder
                    .append("\n\u25AB **Features**: ")
                    .append(guild.features().joinToString("`, `", "`", "`"))
            }

            footer(
                "Count: ${guild.catnip().cache().guilds().size()} | ID: ${guild.id()}",
                guild.catnip().selfUser()!!.effectiveAvatarUrl()
            )
        }
    }

    fun onGuildLeave(guild: Guild) {
        sendEmbed(guild.catnip()) {
            author("AruLog | Lost Server")
            color(Colors.discordRed)
            thumbnail(guild.iconUrl())

            val builder = StringBuilder()

            builder += multiline(
                "\u25AB **Name**: ${guild.name()}",
                "**M/TC/VC**: ${guild.memberCount()}/${guild.channels().count(GuildChannel::isText)}/${guild.channels().count(
                    GuildChannel::isVoice
                )}",
                "\u25AB **Region**: ${guild.region()}",
                "\u25AB **Owner**: ${guild.owner().user().discordTag()}"
            )

            if (guild.features().isNotEmpty()) {
                builder
                    .append("\n\u25AB **Features**: ")
                    .append(guild.features().joinToString("`, `", "`", "`"))
            }

            footer(
                "Count: ${guild.catnip().cache().guilds().size()} | ID: ${guild.id()}",
                guild.catnip().selfUser()!!.effectiveAvatarUrl()
            )
        }
    }

    private fun sendEmbed(catnip: Catnip, builder: EmbedBuilder.() -> Unit) {
        val (id, token) = parseWebhook(webhook)

        catnip.rest().webhook().executeWebhook(
            id, token, MessageOptions().embed(embed(init = builder))
        )
    }
}