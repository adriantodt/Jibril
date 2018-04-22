package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.SUCCESS
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.LinkedBlockingDeque
import javax.inject.Inject

@Command("shuffle")
class Shuffle
@Inject constructor(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteshuffle"), ICommand.HelpDialogProvider {
    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.queue = LinkedBlockingDeque(musicPlayer.queue.shuffled())
        event.channel.sendMessage("$SUCCESS Queue shuffled!").queue()
    }

    override val helpHandler = HelpFactory("Shuffle Command") {
        description(
            "Shuffles the current queue!",
            "",
            "To be able to shuffle the queue, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("voteshuffle", "Create a poll to vote to shuffle the queue.")
    }
}

@Command("voteshuffle")
class VoteShuffle @Inject constructor(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteShuffles

    override fun onVoteAdded(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to shuffle the music has been added. More $votesLeft votes are required to shuffle."
        ).queue()
    }

    override fun onVoteRemoved(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to shuffle the music has been removed. More $votesLeft votes are required to shuffle."
        ).queue()
    }

    override fun onVotesReached(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.queue = LinkedBlockingDeque(musicPlayer.queue.shuffled())
        event.channel.sendMessage("$SUCCESS Enough votes reached! Queue shuffled.").queue()
    }

    override val helpHandler = HelpFactory("VoteShuffle Command") {
        description(
            "Create a poll to shuflle the queue.",
            "",
            "If 60% or more of the users listening vote, the queue will be shuffled."
        )

        alsoSee("shuffle", "Pause the player without requiring voting.")
    }
}