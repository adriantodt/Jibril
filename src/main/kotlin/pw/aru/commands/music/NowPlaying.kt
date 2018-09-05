package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.Description
import pw.aru.core.commands.help.Help
import pw.aru.core.commands.help.SeeAlso
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager

@Command("nowplaying", "np")
@UseFullInjector
class NowPlaying(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        send(musicPlayer.nowPlayingEmbed(currentTrack, event.member)).queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("nowplaying", "np"), "NowPlaying Command", thumbnail = "https://assets.aru.pw/img/aru_music.png"),
        Description("Displays the current track playing"),
        SeeAlso["play", "queue", "repeat"]
    )
}