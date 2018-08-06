@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.CommandPermission
import pw.aru.utils.AruColors
import java.awt.Color

fun EmbedBuilder.baseEmbed(
    event: GuildMessageReceivedEvent,
    name: String,
    url: String? = null,
    image: String? = event.jda.selfUser.effectiveAvatarUrl,
    color: Color? = event.guild.selfMember.color ?: AruColors.primary
) {
    author(name, url, image)
    color(color)
    footer("Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)
}

fun EmbedBuilder.baseEmbed(
    name: String,
    url: String? = null,
    image: String? = null,
    color: Color? = AruColors.primary
) {
    author(name, url, image)
    color(color)
}

fun EmbedBuilder.helpEmbed(
    event: GuildMessageReceivedEvent,
    name: String,
    commandPermission: CommandPermission? = null,
    url: String? = null,
    image: String = event.jda.selfUser.effectiveAvatarUrl,
    color: Color? = event.member.color ?: AruColors.primary
) {
}

fun EmbedBuilder.author(name: String?, url: String? = null, iconUrl: String? = null) {
    setAuthor(name, url, iconUrl)
}

fun EmbedBuilder.color(color: Color?) {
    setColor(color)
}

fun EmbedBuilder.thumbnail(url: String?) {
    setThumbnail(url)
}

fun EmbedBuilder.image(url: String?) {
    setImage(url)
}

fun EmbedBuilder.field(name: String, value: String, inline: Boolean = false) {
    addField(name, value, inline)
}

fun EmbedBuilder.field(name: String, value: Array<String>, inline: Boolean = false) {
    field(name, value.joinToString("\n"), inline)
}

fun EmbedBuilder.description(value: String) {
    setDescription(value)
}

fun EmbedBuilder.description(vararg text: String) {
    setDescription(text.joinToString("\n"))
}

fun EmbedBuilder.footer(text: String?, iconUrl: String? = null) {
    setFooter(text, iconUrl)
}
