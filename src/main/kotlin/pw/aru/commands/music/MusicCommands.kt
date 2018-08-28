package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.list.TLongList
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.prefix
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.core.music.trackData
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.STOP
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X
import pw.aru.utils.extensions.humanUsers

abstract class MusicCommand(val musicManager: MusicManager) : ICommand {
    override val category = Category.MUSIC

    override fun CommandContext.call() {
        val musicPlayer = musicManager.get(event.guild)
        val currentTrack = musicPlayer.currentTrack

        if (currentTrack == null) {
            send(
                "$CONFUSED But I'm not playing anything..."
            ).queue()
            return
        }

        call(musicPlayer, currentTrack)
    }

    abstract fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack)
}

abstract class MusicActionCommand(manager: MusicManager) : MusicCommand(manager) {
    override val category = Category.MUSIC

    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        val voiceState = event.member.voiceState

        if (!voiceState.inVoiceChannel()) {
            send("$X You need to be connected to a Voice Channel to use this command!").queue()
            return
        }

        if (voiceState.channel != musicPlayer.currentChannel) {
            send(
                "$X You're not in the same voice channel as I am..."
            ).queue()
            return
        }

        action(musicPlayer, currentTrack)
    }

    abstract fun CommandContext.action(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack)
}

abstract class MusicPermissionCommand(
    manager: MusicManager,
    private val alternate: String? = null,
    private val userQueued: Boolean = false
) : MusicActionCommand(manager) {
    companion object {
        fun checkPermissions(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, userQueued: Boolean): Boolean {
            // Either:
            // I'm not playing at all or User is the only one listening
            // (If User Queued) User is the one who added the music
            // User is DJ/Server Admin/Bot Developer
            val currentChannel = musicPlayer.currentChannel
            val currentTrack = musicPlayer.currentTrack
            return (currentChannel == null || currentChannel.humanUsers == 1)
                || (userQueued && currentTrack != null && currentTrack.trackData.user == event.author)
                || (CommandPermission.DJ.test(event.member))
        }
    }

    override fun CommandContext.action(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (checkPermissions(event, musicPlayer, userQueued)) {
            actionWithPerms(musicPlayer, currentTrack)
        } else {
            send(
                "$STOP B-baka, I'm not allowed to let you do that!" +
                    if (alternate == null) "" else "\n\n$THINKING Maybe you meant ``$prefix$alternate`` instead?"
            ).queue()
        }
    }

    abstract fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack)
}

abstract class MusicVotingCommand(manager: MusicManager) : MusicActionCommand(manager) {
    open fun CommandContext.checkRequirements(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) = true

    abstract fun getVotes(musicPlayer: GuildMusicPlayer): TLongList
    open fun getRequiredVotes(voiceChannel: VoiceChannel) = (voiceChannel.humanUsers * 0.6).toInt()

    abstract fun CommandContext.onVoteAdded(votesLeft: Int)
    abstract fun CommandContext.onVoteRemoved(votesLeft: Int)
    abstract fun CommandContext.onVotesReached(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String)

    override fun CommandContext.action(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (!checkRequirements(musicPlayer, currentTrack)) return
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
            onVotesReached(musicPlayer, currentTrack, args)
        } else {
            val votesLeft = requiredVotes - votes.size()
            if (removed) {
                onVoteRemoved(votesLeft)
            } else {
                onVoteAdded(votesLeft)
            }
        }
    }
}