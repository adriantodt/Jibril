package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.entities.RepeatMode
import pw.aru.core.music.events.ChangeRepeatModeEvent

@Command("repeat")
class Repeat(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        val mode = when (args) {
            "" -> null
            "none", "disable", "false", "n" -> RepeatMode.NONE
            "song", "music", "current", "playing", "true", "s" -> RepeatMode.SONG
            "queue", "playlist", "list", "q" -> RepeatMode.QUEUE
            else -> return showHelp()
        }

        musicPlayer.publish(ChangeRepeatModeEvent(asMusicSource(), mode))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("repeat"),
            "Repeat Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Sets the repeat mode of the player.",
            "",
            "To be able to set the repeat mode, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        Usage(
            CommandUsage("repeat", "Cycles the Repeat mode between None, Song and Queue."),
            CommandUsage("repeat <none/disable/false/n>", "Disables repeating."),
            CommandUsage("repeat <song/music/current/playing/true/s>", "Repeats the current song."),
            CommandUsage("repeat <queue/playlist/list/q>", "Repeats the current queue.")
        )
    )
}