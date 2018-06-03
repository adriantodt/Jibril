package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.send

@Command("nowplaying", "np")
class NowPlaying
(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.nowPlayingEmbed(currentTrack, event.member).send(event).queue()
    }

    override val helpHandler = HelpFactory("NowPlaying Command") {
        aliases("np")
        description("Displays the current track playing")

        seeAlso("play", "queue", "repeat")
    }
}