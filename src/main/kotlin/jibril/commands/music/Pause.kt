package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.PAUSE
import jibril.utils.emotes.SUCCESS
import jibril.utils.emotes.THINKING
import jibril.utils.emotes.X
import jibril.utils.extensions.withPrefix
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Command("pause")
class Pause(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "votepause"), ICommand.HelpDialogProvider {
    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        if (musicPlayer.audioPlayer.isPaused) {
            event.channel.sendMessage(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``${"resume".withPrefix()}``, instead?"
            ).queue()
            return
        }

        musicPlayer.audioPlayer.isPaused = true
        event.channel.sendMessage("$PAUSE Music Paused.\nType `${"resume".withPrefix()}` to resume the player.").queue()
    }

    override val helpHandler = HelpFactory("Pause Command") {
        description(
            "Pauses the player",
            "",
            "To be able to pause the player, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        alsoSee("votepause", "Create a poll to pause the player.")
        alsoSee("resume", "Resume the player.")
        alsoSee("voteresume", "Create a poll to resume the player.")
    }
}

@Command("votepause")
class VotePause(musicManager: MusicManager) : MusicVotingCommand(musicManager), ICommand.HelpDialogProvider {
    override fun checkRequirements(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String): Boolean {
        if (musicPlayer.audioPlayer.isPaused) {
            event.channel.sendMessage(
                "$X The music is already paused, silly!\n\n$THINKING Maybe you want to resume the music with ``${"voteresume".withPrefix()}``, instead?"
            ).queue()
            return false
        }

        return true
    }

    override fun getVotes(musicPlayer: GuildMusicPlayer) = musicPlayer.votePauses

    override fun onVoteAdded(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to pause the music has been added. More $votesLeft votes are required to pause."
        ).queue()
    }

    override fun onVoteRemoved(event: GuildMessageReceivedEvent, votesLeft: Int) {
        event.channel.sendMessage(
            "$SUCCESS Your vote to pause the music has been removed. More $votesLeft votes are required to pause."
        ).queue()
    }

    override fun onVotesReached(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        musicPlayer.audioPlayer.isPaused = true
        event.channel.sendMessage("$SUCCESS Enough votes reached! Music Paused.\nType `${"voteresume".withPrefix()}` to resume the player.").queue()
    }

    override val helpHandler = HelpFactory("VotePause Command") {
        description(
            "Create a poll to pause the player.",
            "",
            "If 60% or more of the users listening vote, the player will be paused."
        )

        alsoSee("pause", "Pause the player without requiring voting.")
        alsoSee("resume", "Resume the player.")
        alsoSee("voteresume", "Create a poll to resume the player.")
    }
}
