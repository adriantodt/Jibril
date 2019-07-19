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

@Command("votestop")
class VoteStop(musicSystem: MusicSystem) : MusicActionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.publish(ToggleVoteEvent(asMusicSource(), VoteType.STOP))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("votestop"),
            "VoteStop Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Create a poll to stop the current song and clear the queue.",
            "",
            "If 60% or more of the users listening vote, the player will be paused."
        ),
        SeeAlso(
            CommandUsage("stop", "Stops the player without requiring voting.")
        )
    )
}