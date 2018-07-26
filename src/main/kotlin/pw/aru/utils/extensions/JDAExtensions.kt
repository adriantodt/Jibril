@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.EventListener

//Builders

inline fun shardManager(init: DefaultShardManagerBuilder.() -> Unit): ShardManager = DefaultShardManagerBuilder().also(init).build()

inline fun message(message: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit): Message = message.also(init).build()

inline fun embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): MessageEmbed = embed.also(init).build()

inline fun <reified T : Event> listener(noinline onEvent: EventListener.(T) -> Unit) = object : EventListener {
    override fun onEvent(it: Event) {
        if (it is T) onEvent(this, it)
    }
}

//Builders: Assist

inline operator fun DefaultShardManagerBuilder.plusAssign(listener: Any) {
    addEventListeners(listener)
}

inline fun MessageBuilder.embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) {
    setEmbed(embed.also(init).build())
}

//Extras

inline val User.discordTag: String
    get() = "$name#$discriminator"

inline val Member.idLong: Long
    get() = user.idLong

inline val Member.id: String
    get() = user.id

inline val Member.isValid: Boolean
    get() = guild.isMember(user)

inline val VoiceChannel.humanUsers: Int
    get() = members.count { !it.user.isBot }
