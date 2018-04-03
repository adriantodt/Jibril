@file:Suppress("NOTHING_TO_INLINE")

package jibril.utils.extensions

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import javax.security.auth.login.LoginException

//Builders

inline fun shardManager(init: DefaultShardManagerBuilder.() -> Unit): ShardManager = with(DefaultShardManagerBuilder()) {
    init()
    build()
}

inline fun embed(embed: EmbedBuilder = EmbedBuilder(), init: EmbedBuilder.() -> Unit): MessageEmbed = with(embed) {
    init()
    build()
}

inline fun message(message: MessageBuilder = MessageBuilder(), init: MessageBuilder.() -> Unit): Message? = with(message) {
    init()
    build()
}

//Member

val Member.idLong: Long
    get() = user.idLong

//Extras

inline fun MessageEmbed.send(e: GuildMessageReceivedEvent) = send(e.channel)

inline fun MessageEmbed.send(c: MessageChannel): MessageAction = c.sendMessage(this)

val User.discordTag: String
    get() = "$name#$discriminator"

val Member.isValid: Boolean
    get() = guild.isMember(user)

val VoiceChannel.humanUsers: Int
    get() {
        return members.filter { !it.user.isBot }.size
    }

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