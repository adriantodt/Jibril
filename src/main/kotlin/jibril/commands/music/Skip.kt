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

@Command("skip")
class Skip @Inject constructor(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteskip", true), ICommand.HelpDialogProvider {

    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        event.channel.sendMessage(
            "$SUCCESS Skipping this song..."
        ).queue()
        musicPlayer.startNext(true)
    }

    override val helpHandler = HelpFactory("Skip Command") {
        description(
            "Skips the current song!",
            "",
            "To be able to skip the song, you have to:",
            "- Be the user who added the current music",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("voteskip", "Create a poll to vote to skip the current track.")
    }
}

@Command("voteskip")
class VoteSkip @Inject constructor(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteSkips

    override fun onVoteAdded(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to skip the music has been added. More $votesLeft votes are required to skip."
        ).queue()
    }

    override fun onVoteRemoved(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to skip the music has been removed. More $votesLeft votes are required to skip."
        ).queue()
    }

    override fun onVotesReached(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        event.channel.sendMessage("$SUCCESS Enough votes reached! Skipping this song...").queue()
        musicPlayer.startNext(true)
    }

    override val helpHandler = HelpFactory("VoteSkip Command") {
        description(
            "Create a poll to skip the current track.",
            "",
            "If 60% or more of the users listening vote, the current track will be skipped."
        )

        alsoSee("skip", "Skips the track without needing voting.")
    }
}