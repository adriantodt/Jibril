package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.SUCCESS
import jibril.utils.extensions.showHelp
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Command("repeat")
class Repeat
(
    musicManager: MusicManager
) : MusicPermissionCommand(musicManager), ICommand.HelpDialogProvider {
    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
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

        event.channel.sendMessage(
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