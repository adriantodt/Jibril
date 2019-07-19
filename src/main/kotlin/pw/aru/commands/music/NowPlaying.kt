package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.Description
import pw.aru.bot.commands.help.Help
import pw.aru.bot.commands.help.SeeAlso
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.utils.NowPlayingEmbed.nowPlayingEmbed
import pw.aru.commands.music.base.MusicCommand

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