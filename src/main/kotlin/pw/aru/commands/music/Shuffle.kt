package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.utils.text.SUCCESS
import java.util.concurrent.LinkedBlockingDeque

@Command("shuffle")
class Shuffle(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "voteshuffle"),
    ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.queue = LinkedBlockingDeque(musicPlayer.queue.shuffled())
        send("$SUCCESS Queue shuffled!")
    }

    override val helpHandler = Help(
        CommandDescription(listOf("shuffle"), "Shuffle Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
        Description(
            "Shuffles the current queue!",
            "",
            "To be able to shuffle the queue, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteshuffle", "Create a poll to vote to shuffle the queue.")
        )
    )
}

