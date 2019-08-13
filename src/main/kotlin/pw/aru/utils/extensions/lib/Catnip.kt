package pw.aru.utils.extensions.lib

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.channel.MessageChannel
import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Embed
import com.mewna.catnip.entity.message.MessageOptions
import com.mewna.catnip.entity.user.VoiceState
import io.vertx.core.eventbus.MessageConsumer
import java.io.Closeable

inline fun message(init: MessageOptions.() -> Unit): MessageOptions =
    MessageOptions().also(init)

inline fun embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): Embed = embed.also(init).build()

inline fun MessageOptions.embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) {
    embed(embed.also(init).build())
}

inline fun MessageChannel.sendEmbed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) =
    sendMessage(embed.also(init).build())

inline fun MessageChannel.sendMessage(init: MessageOptions.() -> Unit) =
    sendMessage(message(init))


fun EmbedBuilder.footer(text: String) {
    footer(text, null)
}

fun EmbedBuilder.field(name: String, vararg value: String) {
    field(name, value.joinToString("\n"), false)
}

fun EmbedBuilder.inlineField(name: String, vararg value: String) {
    field(name, value.joinToString("\n"), true)
}

fun EmbedBuilder.description(vararg text: String) {
    description(text.joinToString("\n"))
}

fun EmbedBuilder.blankField(inline: Boolean = false) {
    field("\u200E", "\u200E", inline)
}

inline val VoiceChannel.listeners: Collection<VoiceState>
    get() = guild().voiceChannelListeners(idAsLong())

inline val VoiceChannel.humanUsersCount: Int
    get() = guild().voiceChannelHumanCount(idAsLong())

fun Guild.voiceChannelListeners(id: Long): Collection<VoiceState> {
    return voiceStates().find { it.channelIdAsLong() == id }
}

fun Guild.voiceChannelHumanCount(id: Long): Int {
    return voiceStates().count { it.channelIdAsLong() == id && !it.user().bot() }.toInt()
}

fun Member.voiceState(): VoiceState? = guild().voiceStates().getById(idAsLong())

fun <T> MessageConsumer<T>.asCloseable(): Closeable {
    return Closeable {
        this.unregister()
    }
}
