package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Help
import pw.aru.core.commands.help.SeeAlso
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.utils.NowPlayingEmbed.nowPlayingEmbed

@Command("nowplaying", "np")
class NowPlaying(musicSystem: MusicSystem) : MusicCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.call(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.textChannel?.idAsLong() == channel.idAsLong()) {
            musicPlayer.sendNowPlaying(author)
        } else {
            send(nowPlayingEmbed(musicPlayer, author))
        }
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("nowplaying", "np"),
            "NowPlaying Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description("Displays the current track playing"),
        SeeAlso["play", "queue", "repeat"]
    )
}