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

@Command("voteshuffle")
class VoteShuffle(musicSystem: MusicSystem) : MusicActionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.publish(ToggleVoteEvent(asMusicSource(), VoteType.SHUFFLE))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("voteshuffle"),
            "VoteShuffle Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Create a poll to shuffle the queue.",
            "",
            "If 60% or more of the users listening vote, the queue will be shuffled."
        ),
        SeeAlso(
            CommandUsage("shuffle", "Shuffles the queue without requiring voting.")
        )
    )
}