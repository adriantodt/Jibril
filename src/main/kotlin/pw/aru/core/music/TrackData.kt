package pw.aru.core.music

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User

class TrackData(channel: TextChannel, user: User) {
    private val channelId: Long = channel.idLong
    private val userId: Long = user.idLong
    val jda: JDA = channel.jda
    var messageId: Long = 0

    var thumbnail: String? = null

    val textChannel: TextChannel?
        get() = jda.getTextChannelById(channelId)

    val user: User?
        get() = jda.getUserById(userId)
}
