package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.entities.PauseState
import pw.aru.bot.music.events.ChangePauseStateEvent
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.utils.text.THINKING
import pw.aru.utils.text.X

@Command("pause")
class Pause(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "votepause"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.andePlayer.paused()) {
            send(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``$prefix${"resume"}``, instead?"
            )
            return
        }

        musicPlayer.publish(ChangePauseStateEvent(asMusicSource(), PauseState.PAUSED))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("pause"),
            "Pause Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Pauses the player",
            "",
            "To be able to pause the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("votepause", "Create a poll to pause the player."),
            CommandUsage("resume", "Resume the player."),
            CommandUsage("voteresume", "Create a poll to resume the player.")
        )
    )
}

