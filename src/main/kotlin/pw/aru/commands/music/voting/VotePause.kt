package pw.aru.commands.music.voting

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicActionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.entities.VoteType
import pw.aru.core.music.events.ToggleVoteEvent
import pw.aru.utils.text.THINKING
import pw.aru.utils.text.X

@Command("votepause")
class VotePause(musicSystem: MusicSystem) : MusicActionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.lavaPlayer.paused()) {
            send(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``$prefix${"voteresume"}``, instead?"
            )
            return
        }

        musicPlayer.publish(ToggleVoteEvent(asMusicSource(), VoteType.PAUSE))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("votepause"),
            "VotePause Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Create a poll to pause the player.",
            "",
            "If 60% or more of the users listening vote, the player will be paused."
        ),
        SeeAlso(
            CommandUsage("pause", "Pause the player without requiring voting."),
            CommandUsage("resume", "Resume the player."),
            CommandUsage("voteresume", "Create a poll to resume the player.")
        )
    )
}