package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.events.StopMusicEvent
import pw.aru.utils.text.SUCCESS

@Command("stop")
class Stop(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "votestop"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        val size = musicPlayer.queue.size

        musicPlayer.publish(StopMusicEvent(asMusicSource()))

        send(
            "$SUCCESS Stopped the current track and removed $size tracks from the queue."
        )
    }

    override val helpHandler = Help(
        CommandDescription(listOf("stop"), "Stop Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
        Description(
            "Stops the current song and clear the queue.",
            "",
            "To be able to stop the music, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("votestop", "Create a poll to vote to stop the music.")
        )
    )
}

