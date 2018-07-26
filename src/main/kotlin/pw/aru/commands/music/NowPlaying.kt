package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.send

@Command("nowplaying", "np")
@UseFullInjector
class NowPlaying(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.nowPlayingEmbed(currentTrack, event.member).send(event).queue()
    }

    override val helpHandler = HelpFactory("NowPlaying Command") {
        aliases("np")
        description("Displays the current track playing")

        seeAlso("play", "queue", "repeat")
    }
}