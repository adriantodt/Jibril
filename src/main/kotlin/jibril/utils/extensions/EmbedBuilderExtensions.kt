package jibril.utils.extensions

import jibril.core.commands.CommandPermission
import jibril.utils.Colors
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

fun EmbedBuilder.baseEmbed(
    event: GuildMessageReceivedEvent,
    name: String,
    url: String? = null,
    image: String? = event.jda.selfUser.effectiveAvatarUrl,
    color: Color? = event.guild.selfMember.color ?: Colors.jibrilPrimary
) {
    author(name, url, image)
    color(color)
    footer("Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)
}

fun EmbedBuilder.baseEmbed(
    name: String,
    url: String? = null,
    image: String? = null,
    color: Color? = Colors.jibrilPrimary
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
    color: Color? = event.member.color ?: Colors.jibrilPrimary
) {
    baseEmbed(event, name, url, image, color)
    thumbnail("https://i.imgur.com/a5lJho6.png")
    if (commandPermission != null) field("Permission Required:", commandPermission.toString(), false)
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
