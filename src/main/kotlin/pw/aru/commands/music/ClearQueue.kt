package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.PLAY
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X
import pw.aru.utils.extensions.withPrefix

@Command("clearqueue")
@UseFullInjector
class ClearQueue(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteclearqueue"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.queue.isEmpty()) {
            send(
                "$X The queue is already empty, silly!\n\n$THINKING Maybe you want to skip the current song with ``${"skip".withPrefix()}``, instead?"
            ).queue()
            return
        }

        musicPlayer.queue.clear()
        send("$PLAY Queue cleared.").queue()
    }

    override val helpHandler = HelpFactory("ClearQueue Command") {
        description(
            "Clears the player's queue.",
            "",
            "To be able to clear the player's queue, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("voteclearqueue", "Create a poll to clear the player's queue.")
        alsoSee("skip", "Skips the current song.")
        alsoSee("voteskip", "Create a poll to skip the current song.")
    }
}

@Command("voteclearqueue")
@UseFullInjector
class VoteClearQueue(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.checkRequirements(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack): Boolean {
        if (musicPlayer.queue.isEmpty()) {
            send(
                "$X The queue is already empty, silly!\n\n$THINKING Maybe you want to skip the current song with ``${"voteskip".withPrefix()}``, instead?"
            ).queue()
            return false
        }

        return true
    }

    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteClearQueue

    override fun CommandContext.onVoteAdded(votesLeft: Int) {
        send("$SUCCESS Your vote to clear the player's queue has been added. More $votesLeft votes are required to clear the player's queue.").queue()
    }

    override fun CommandContext.onVoteRemoved(votesLeft: Int) {
        send("$SUCCESS Your vote to clear the player's queue has been removed. More $votesLeft votes are required to clear the player's queue.").queue()
    }

    override fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.queue.clear()
        send("$SUCCESS Enough votes reached! Queue cleared.").queue()
    }

    override val helpHandler = HelpFactory("VoteClearQueue Command") {
        description(
            "Create a poll to clear the player's queue.",
            "",
            "If 60% or more of the users listening vote, the player's queue will be queued."
        )

        alsoSee("clearqueue", "Clears the queue of the player without requiring voting.")
        alsoSee("skip", "Skips the current song.")
        alsoSee("voteskip", "Create a poll to skip the current song.")
    }
}