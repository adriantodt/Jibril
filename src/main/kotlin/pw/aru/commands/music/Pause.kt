package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.emotes.PAUSE
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X

@Command("pause")
@UseFullInjector
class Pause(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "votepause"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.audioPlayer.isPaused) {
            send(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``$prefix${"resume"}``, instead?"
            ).queue()
            return
        }

        musicPlayer.audioPlayer.isPaused = true
        send("$PAUSE Music Paused.\nType `$prefix${"resume"}` to resume the player.").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("pause"), "Pause Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
        Description(
            "Pauses the player",
            "",
            "To be able to pause the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("votepause", "Create a poll to pause the player."),
            CommandUsage("resume", "Resume the player."),
            CommandUsage("voteresume", "Create a poll to resume the player.")
        )
    )
}

@Command("votepause")
@UseFullInjector
class VotePause(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.checkRequirements(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack): Boolean {
        if (musicPlayer.audioPlayer.isPaused) {
            send(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``$prefix${"voteresume"}``, instead?"
            ).queue()
            return false
        }

        return true
    }

    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.votePauses

    override fun CommandContext.onVoteAdded(votesLeft: Int) {
        send("$SUCCESS Your vote to pause the music has been added. More $votesLeft votes are required to pause.").queue()
    }

    override fun CommandContext.onVoteRemoved(votesLeft: Int) {
        send("$SUCCESS Your vote to pause the music has been removed. More $votesLeft votes are required to pause.").queue()
    }

    override fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.audioPlayer.isPaused = true
        send("$SUCCESS Enough votes reached! Music Paused.\nType `$prefix${"voteresume"}` to resume the player.").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("votepause"), "VotePause Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
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
