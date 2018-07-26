package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.SUCCESS

@Command("repeat")
@UseFullInjector
class Repeat(musicManager: MusicManager) : MusicPermissionCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        val mode = when (args) {
            "" -> {
                musicPlayer.repeatMode.cycleNext()
            }
            "none", "disable", "false", "n" -> {
                GuildMusicPlayer.RepeatMode.NONE
            }
            "song", "music", "current", "playing", "true", "s" -> {
                GuildMusicPlayer.RepeatMode.SONG
            }
            "queue", "playlist", "list", "q" -> {
                GuildMusicPlayer.RepeatMode.QUEUE
            }
            else -> return showHelp()
        }

        musicPlayer.repeatMode = mode

        send(
            "$SUCCESS Repeat mode set to `${mode.name.toLowerCase()}`!"
        ).queue()
    }

    override val helpHandler = HelpFactory("Repeat Command") {
        description(
            "Sets the repeat mode of the player.",
            "",
            "To be able to set the repeat mode, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        usage("repeat", "Cycles the Repeat mode between None, Song and Queue.")
        usage("repeat <none/disable/false/n>", "Disables repeating.")
        usage("repeat <song/music/current/playing/true/s>", "Repeats the current song.")
        usage("repeat <queue/playlist/list/q>", "Repeats the current queue.")
    }
}