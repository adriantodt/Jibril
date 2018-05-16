package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.extensions.send
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

@Command("nowplaying", "np")
class NowPlaying
@Inject constructor(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.nowPlayingEmbed(currentTrack, event.member).send(event).queue()
    }

    override val helpHandler = HelpFactory("NowPlaying Command") {
        aliases("np")
        description("Displays the current track playing")

        seeAlso("play", "queue", "repeat")
    }
}