package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.entities.PauseState
import pw.aru.core.music.events.ChangePauseStateEvent
import pw.aru.utils.text.THINKING
import pw.aru.utils.text.X

@Command("resume")
class Resume(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "voteresume"),
    ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (!musicPlayer.lavaPlayer.paused()) {
            send(
                "$X The music is already playing, silly!\n\n$THINKING Maybe you want to pause the music with ``$prefix${"pause"}``, instead?"
            )
            return
        }

        musicPlayer.publish(ChangePauseStateEvent(asMusicSource(), PauseState.RESUMED))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("resume"),
            "Resume Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Resumes the player",
            "",
            "To be able to resume the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteresume", "Create a poll to resume the player."),
            CommandUsage("pause", "Pause the player."),
            CommandUsage("votepause", "Create a poll to pause the player.")
        )
    )
}

