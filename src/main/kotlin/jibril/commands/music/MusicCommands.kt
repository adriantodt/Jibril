package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.list.TLongList
import jibril.core.categories.Categories
import jibril.core.commands.CommandPermission
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.core.music.trackData
import jibril.utils.emotes.CONFUSED
import jibril.utils.emotes.STOP
import jibril.utils.emotes.THINKING
import jibril.utils.emotes.X
import jibril.utils.extensions.humanUsers
import jibril.utils.extensions.withPrefix
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

abstract class MusicCommand(val musicManager: MusicManager) : ICommand {
    override val category = Categories.MUSIC

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        val musicPlayer = musicManager.getMusicPlayer(event.guild)
        val currentTrack = musicPlayer.currentTrack

        if (currentTrack == null) {
            event.channel.sendMessage(
                "$CONFUSED But I'm not playing anything..."
            ).queue()
            return
        }

        run(event, musicPlayer, currentTrack, args)
    }

    abstract fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String)
}

abstract class MusicActionCommand(manager: MusicManager) : MusicCommand(manager) {
    override val category = Categories.MUSIC

    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val voiceState = event.member.voiceState

        if (!voiceState.inVoiceChannel()) {
            event.channel.sendMessage("$X You need to be connected to a Voice Channel to use this command!").queue()
            return
        }

        if (voiceState.channel != musicPlayer.currentChannel) {
            event.channel.sendMessage(
                "$X You're not in the same voice channel as I am..."
            ).queue()
            return
        }

        call(event, musicPlayer, currentTrack, args)
    }

    abstract fun call(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String)
}

abstract class MusicPermissionCommand(
    manager: MusicManager,
    private val alternate: String? = null,
    private val userQueued: Boolean = false
) : MusicActionCommand(manager) {
    override fun call(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        if (userQueued) {
            //User is the one who added the music
            if (currentTrack.trackData.user == event.author) {
                return action(event, musicPlayer, currentTrack, args)
            }
        }

        //User is the only one listening
        if (musicPlayer.currentChannel!!.humanUsers == 1) {
            return action(event, musicPlayer, currentTrack, args)
        }

        //User is DJ/Server Admin/Bot Developer
        if (!CommandPermission.DJ.test(event.member)) {
            event.channel.sendMessage(
                "$STOP B-baka, I'm not allowed to let you do that!" +
                    if (alternate == null) "" else "\n\n$THINKING Maybe you meant ``${alternate.withPrefix()}`` instead?"
            ).queue()
            return
        }

        action(event, musicPlayer, currentTrack, args)
    }

    abstract fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String)
}

abstract class MusicVotingCommand(manager: MusicManager) : MusicActionCommand(manager) {
    open fun getRequiredVotes(voiceChannel: VoiceChannel) = (voiceChannel.humanUsers * 0.6).toInt()
    open fun checkRequirements(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) = true
    abstract fun getVotes(musicPlayer: GuildMusicPlayer): TLongList

    abstract fun onVoteAdded(event: GuildMessageReceivedEvent, votesLeft: Int)
    abstract fun onVoteRemoved(event: GuildMessageReceivedEvent, votesLeft: Int)
    abstract fun onVotesReached(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String)

    override fun call(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {

        if (!checkRequirements(event, musicPlayer, currentTrack, args)) return
        val votes = getVotes(musicPlayer)
        val voiceChannel = musicPlayer.currentChannel!!
        val requiredVotes = getRequiredVotes(voiceChannel)

        val removed = votes.contains(event.author.idLong)

        if (removed) {
            votes.remove(event.author.idLong)
        } else {
            votes.add(event.author.idLong)
        }

        if (votes.size() >= requiredVotes) {
            votes.clear()
            onVotesReached(event, musicPlayer, currentTrack, args)
        } else {
            val votesLeft = requiredVotes - votes.size()
            if (removed) {
                onVoteRemoved(event, votesLeft)
            } else {
                onVoteAdded(event, votesLeft)
            }
        }
    }
}