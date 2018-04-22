package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.SUCCESS
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

@Command("stop")
class Stop @Inject constructor(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "votestop"), ICommand.HelpDialogProvider {

    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val size = musicPlayer.queue.size

        musicPlayer.stop()

        event.channel.sendMessage(
            "$SUCCESS Stopped the current track and removed $size tracks from the queue."
        ).queue()
    }

    override val helpHandler = HelpFactory("Stop Command") {
        description(
            "Stops the current song and clear the queue.",
            "",
            "To be able to stop the music, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("votestop", "Create a poll to vote to stop the music.")
    }
}

@Command("votestop")
class VoteStop @Inject constructor(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteStops

    override fun onVoteAdded(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to stop the music has been added. More $votesLeft votes are required to stop."
        ).queue()
    }

    override fun onVoteRemoved(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to stop the music has been removed. More $votesLeft votes are required to stop."
        ).queue()
    }

    override fun onVotesReached(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val size = musicPlayer.queue.size

        musicPlayer.stop()

        event.channel.sendMessage(
            "$SUCCESS Enough votes reached! Stopped the current track and removed $size tracks from the queue."
        ).queue()
    }

    override val helpHandler = HelpFactory("VoteStop Command") {
        description(
            "Create a poll to stop the current song and clear the queue.",
            "",
            "If 60% or more of the users listening vote, the player will be paused."
        )

        alsoSee("stop", "Stops the player without needing voting.")
    }
}