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

@Command("skip")
@UseFullInjector
class Skip(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteskip", true), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        send(
            "$SUCCESS Skipping this song..."
        ).queue()
        musicPlayer.startNext(true)
    }

    override val helpHandler = Help(
        CommandDescription(listOf("skip"), "Skip Command"),
        Description(
            "Skips the current song!",
            "",
            "To be able to skip the song, you have to:",
            "- Be the user who added the current music",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteskip", "Create a poll to vote to skip the current track.")
        )
    )
}

@Command("voteskip")
@UseFullInjector
class VoteSkip(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.voteSkips

    override fun CommandContext.onVoteAdded(votesLeft: Int) {
        send(
            "$SUCCESS Your vote to skip the music has been added. More $votesLeft votes are required to skip."
        ).queue()
    }

    override fun CommandContext.onVoteRemoved(votesLeft: Int) {
        send(
            "$SUCCESS Your vote to skip the music has been removed. More $votesLeft votes are required to skip."
        ).queue()
    }

    override fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        send("$SUCCESS Enough votes reached! Skipping this song...").queue()
        musicPlayer.startNext(true)
    }

    override val helpHandler = Help(
        CommandDescription(listOf("voteskip"), "VoteSkip Command"),
        Description(
            "Create a poll to skip the current track.",
            "",
            "If 60% or more of the users listening vote, the current track will be skipped."
        ),
        SeeAlso(
            CommandUsage("skip", "Skips the track without requiring voting.")
        )
    )
}