package pw.aru.commands.music.voting

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.entities.VoteType
import pw.aru.bot.music.events.ToggleVoteEvent
import pw.aru.commands.music.base.MusicActionCommand

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