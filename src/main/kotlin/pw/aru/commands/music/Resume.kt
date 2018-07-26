package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
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

@Command("resume")
@UseFullInjector
class Resume(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteresume"), ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (!musicPlayer.audioPlayer.isPaused) {
            event.channel.sendMessage(
                "$X The music is already playing, silly!\n\n$THINKING Maybe you want to pause the music with ``${"pause".withPrefix()}``, instead?"
            ).queue()
            return
        }

        musicPlayer.audioPlayer.isPaused = false
        event.channel.sendMessage("$PLAY Music resumed.").queue()
    }

    override val helpHandler = HelpFactory("Resume Command") {
        description(
            "Resumes the player",
            "",
            "To be able to resume the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("voteresume", "Create a poll to resume the player.")
        alsoSee("pause", "Pause the player.")
        alsoSee("votepause", "Create a poll to pause the player.")
    }
}

@Command("voteresume")
@UseFullInjector
class VoteResume(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun checkRequirements(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String): Boolean {
        if (!musicPlayer.audioPlayer.isPaused) {
            event.channel.sendMessage(
                "$X The music is already playing, silly!\n\n$THINKING Maybe you want to pause the music with ``${"votepause".withPrefix()}``, instead?"
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

    override val helpHandler = HelpFactory("VoteResume Command") {
        description(
            "Create a poll to resume the player.",
            "",
            "If 60% or more of the users listening vote, the player will be resumed."
        )

        alsoSee("resume", "Resume the player without requiring voting.")
        alsoSee("pause", "Pause the player.")
        alsoSee("votepause", "Create a poll to pause the player.")
    }
}