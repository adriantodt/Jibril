package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.emotes.PLAY
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X

@Command("resume")
@UseFullInjector
class Resume(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteresume"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (!musicPlayer.audioPlayer.isPaused) {
            send(
                "$X The music is already playing, silly!\n\n$THINKING Maybe you want to pause the music with ``$prefix${"pause"}``, instead?"
            ).queue()
            return
        }

        musicPlayer.audioPlayer.isPaused = false
        send("$PLAY Music resumed.").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("resume"), "Resume Command", thumbnail = "https://assets.aru.pw/img/aru_music.png"),
        Description(
            "Resumes the player",
            "",
            "To be able to resume the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        SeeAlso(
            CommandUsage("voteresume", "Create a poll to resume the player."),
            CommandUsage("pause", "Pause the player."),
            CommandUsage("votepause", "Create a poll to pause the player.")
        )
    )
}

@Command("voteresume")
@UseFullInjector
class VoteResume(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.checkRequirements(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack): Boolean {
        if (!musicPlayer.audioPlayer.isPaused) {
            send(
                "$X The music is already playing, silly!\n\n$THINKING Maybe you want to pause the music with ``$prefix${"votepause"}``, instead?"
            ).queue()
            return false
        }

        return true
    }

    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.votePauses

    override fun CommandContext.onVoteAdded(votesLeft: Int) {
        send("$SUCCESS Your vote to resume the music has been added. More $votesLeft votes are required to resume.").queue()
    }

    override fun CommandContext.onVoteRemoved(votesLeft: Int) {
        send("$SUCCESS Your vote to resume the music has been removed. More $votesLeft votes are required to resume.").queue()
    }

    override fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.audioPlayer.isPaused = false
        send("$SUCCESS Enough votes reached! Music resumed.").queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("voteresume"), "VoteResume Command", thumbnail = "https://assets.aru.pw/img/aru_music.png"),
        Description(
            "Create a poll to resume the player.",
            "",
            "If 60% or more of the users listening vote, the player will be resumed."
        ),
        SeeAlso(
            CommandUsage("resume", "Resume the player without requiring voting."),
            CommandUsage("pause", "Pause the player."),
            CommandUsage("votepause", "Create a poll to pause the player.")
        )
    )
}