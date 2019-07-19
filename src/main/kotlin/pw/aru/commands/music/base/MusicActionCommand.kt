package pw.aru.commands.music.base

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.categories.Category
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.utils.text.X

abstract class MusicActionCommand(manager: MusicSystem) : MusicCommand(manager) {
    override val category = Category.MUSIC

    override fun CommandContext.call(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        val voiceState = guild.voiceStates().getById(author.idAsLong())

        if (voiceState.channelIdAsLong() == 0L) {
            send("$X You need to be connected to a Voice Channel to use this command!")
            return
        }

        if (voiceState.channel() != musicPlayer.voiceChannel) {
            send(
                "$X You're not in the same voice channel as I am..."
            )
            return
        }

        action(musicPlayer, currentTrack)
    }

    abstract fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack)
}