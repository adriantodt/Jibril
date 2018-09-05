package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.emotes.SUCCESS

@Command("stop")
@UseFullInjector
class Stop(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "votestop"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        val size = musicPlayer.queue.size

        musicPlayer.stop()

        send(
            "$SUCCESS Stopped the current track and removed $size tracks from the queue."
        ).queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("stop"), "Stop Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
        Description(
            "Stops the current song and clear the queue.",
            "",
            "To be able to stop the music, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("votestop", "Create a poll to vote to stop the music.")
        )
    )
}

@Command("votestop")
@UseFullInjector
class VoteStop(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteStops

    override fun CommandContext.onVoteAdded(votesLeft: Int) {
        send(
            "$SUCCESS Your vote to stop the music has been added. More $votesLeft votes are required to stop."
        ).queue()
    }

    override fun CommandContext.onVoteRemoved(votesLeft: Int) {
        send(
            "$SUCCESS Your vote to stop the music has been removed. More $votesLeft votes are required to stop."
        ).queue()
    }

    override fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val size = musicPlayer.queue.size

        musicPlayer.stop()

        send(
            "$SUCCESS Enough votes reached! Stopped the current track and removed $size tracks from the queue."
        ).queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("votestop"), "VoteStop Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
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