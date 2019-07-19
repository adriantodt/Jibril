package pw.aru.commands.music.base

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.utils.text.CONFUSED

abstract class MusicCommand(val musicSystem: MusicSystem) : ICommand {
    override val category = Category.MUSIC

    override fun CommandContext.call() {
        val musicPlayer: MusicPlayer? = musicSystem.players[guild.idAsLong()]
        val currentTrack = musicPlayer?.currentTrack

        if (currentTrack == null) {
            send(
                "$CONFUSED But I'm not playing anything..."
            )
            return
        }

        call(musicPlayer, currentTrack)
    }

    abstract fun CommandContext.call(musicPlayer: MusicPlayer, currentTrack: AudioTrack)
}