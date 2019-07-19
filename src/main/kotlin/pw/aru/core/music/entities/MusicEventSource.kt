package pw.aru.core.music.entities

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.user.User


sealed class MusicEventSource {
    abstract fun member(guild: Guild): Member?
    abstract fun channel(guild: Guild): TextChannel?

    object AndesiteNode : MusicEventSource() {
        override fun member(guild: Guild): Nothing? = null
        override fun channel(guild: Guild): Nothing? = null
    }

    object MusicSystem : MusicEventSource() {
        override fun member(guild: Guild): Nothing? = null
        override fun channel(guild: Guild): Nothing? = null
    }

    object VotingSystem : MusicEventSource() {
        override fun member(guild: Guild): Nothing? = null
        override fun channel(guild: Guild): Nothing? = null
    }

    data class Dashboard(val userId: Long) : MusicEventSource() {
        override fun member(guild: Guild): Member? = guild.members().getById(userId)
        override fun channel(guild: Guild): Nothing? = null
    }

    data class Discord(val user: User, val textChannel: TextChannel) : MusicEventSource() {
        override fun member(guild: Guild): Member? = guild.member(user.idAsLong())
        override fun channel(guild: Guild): TextChannel? = textChannel
    }
}
