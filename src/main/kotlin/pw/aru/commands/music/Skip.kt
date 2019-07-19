package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.events.SkipTrackEvent
import pw.aru.commands.music.base.MusicPermissionCommand

@Command("skip")
class Skip(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "voteskip", true),
    ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.publish(SkipTrackEvent(asMusicSource()))
    }

    override val helpHandler = Help(
        CommandDescription(listOf("skip"), "Skip Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
        Description(
            "Skips the current song!",
            "",
            "To be able to skip the song, you have to:",
            "- Be the user who added the current music",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteskip", "Create a poll to vote to skip the current track.")
        )
    )
}

