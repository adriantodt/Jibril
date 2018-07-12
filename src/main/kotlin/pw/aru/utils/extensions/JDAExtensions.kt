@file:Suppress("NOTHING_TO_INLINE")

package pw.aru.utils.extensions

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.requests.restaction.MessageAction
import javax.security.auth.login.LoginException

//Builders

inline fun shardManager(init: DefaultShardManagerBuilder.() -> Unit): ShardManager = with(DefaultShardManagerBuilder()) {
    init()
    build()
}

inline fun message(message: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit) = with(message) {
    init()
    build()
}

inline fun MessageBuilder.embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit) {
    setEmbed(embed.also(init).build())
}

inline fun embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): MessageEmbed = with(embed) {
    init()
    build()
}

//Builders: Assist

inline operator fun DefaultShardManagerBuilder.plusAssign(listener: Any) {
    addEventListeners(listener)
}


//Member

inline val Member.idLong: Long
    get() = user.idLong

inline val Member.id: String
    get() = user.id

//Extras

inline fun <reified T : Event> listener(noinline onEvent: EventListener.(T) -> Unit) = object : EventListener {
    override fun onEvent(it: Event) {
        if (it is T) onEvent(this, it)
    }
}

inline fun MessageEmbed.send(e: GuildMessageReceivedEvent) = send(e.channel)

inline fun MessageEmbed.send(c: MessageChannel): MessageAction = c.sendMessage(this)

inline fun Message.send(e: GuildMessageReceivedEvent) = send(e.channel)

inline fun Message.send(c: MessageChannel): MessageAction = c.sendMessage(this)

inline val User.discordTag: String
    get() = "$name#$discriminator"

inline val Member.isValid: Boolean
    get() = guild.isMember(user)

inline val VoiceChannel.humanUsers: Int
    get() = members.count { !it.user.isBot }

@Throws(LoginException::class, InterruptedException::class)
fun JDA.blockUntil(status: JDA.Status): JDA {
    check(status.isInit) {
        "Cannot await the status $status as it is not part of the login cycle!"
    }

    while (!this.status.isInit || this.status.ordinal < status.ordinal) {
        check(this.status != JDA.Status.SHUTDOWN) { "JDA was unable to finish starting up!" }
        Thread.sleep(50)
    }

    return this
}

inline fun ShardManager.getShardForGuild(guildId: String): JDA {
    return getShardForGuild(guildId.toLong())
}

inline fun ShardManager.getShardForGuild(guildId: Long): JDA {
    return getShardById(((guildId shr 22) % shardsTotal).toInt())
}