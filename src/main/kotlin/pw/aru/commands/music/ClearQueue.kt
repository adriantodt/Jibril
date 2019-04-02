package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.events.ClearQueueEvent
import pw.aru.utils.text.PLAY
import pw.aru.utils.text.THINKING
import pw.aru.utils.text.X

@Command("clearqueue", "cq")
class ClearQueue(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "voteclearqueue"),
    ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.queue.isEmpty()) {
            send(
                "$X The queue is already empty, silly!\n\n$THINKING Maybe you want to skip the current song with ``$prefix${"skip"}``, instead?"
            )
            return
        }

        musicPlayer.publish(ClearQueueEvent(asMusicSource()))

        send("$PLAY Queue cleared.")
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("clearqueue"),
            "ClearQueue Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Clears the player's queue.",
            "",
            "To be able to clear the player's queue, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteclearqueue", "Create a poll to clear the player's queue."),
            CommandUsage("skip", "Skips the current song."),
            CommandUsage("voteskip", "Create a poll to skip the current song.")
        )
    )
}

