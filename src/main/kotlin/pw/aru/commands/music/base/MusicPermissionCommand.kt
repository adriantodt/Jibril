package pw.aru.commands.music.base

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.prefix
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.core.permissions.MemberPermissions
import pw.aru.utils.extensions.lang.anyOf
import pw.aru.utils.extensions.lib.humanUsersCount
import pw.aru.utils.text.STOP
import pw.aru.utils.text.THINKING

abstract class MusicPermissionCommand(
    manager: MusicSystem,
    private val alternate: String? = null,
    private val userQueued: Boolean = false
) : MusicActionCommand(manager) {
    companion object {
        fun CommandContext.checkPermissions(musicPlayer: MusicPlayer, userQueued: Boolean): Boolean {
            // Either:
            // I'm not playing at all or User is the only one listening
            // (If User Queued) User is the one who added the music
            // User is DJ/Server Admin/Bot Developer
            val currentChannel = musicPlayer.voiceChannel
            val currentTrackData = musicPlayer.lastTrackData

            return anyOf(
                currentChannel?.humanUsersCount?.equals(1) ?: true,
                userQueued && currentTrackData?.source?.member(guild)?.equals(author) ?: true,
                permissions.contains(MemberPermissions.DJ)
            )
        }
    }

    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (checkPermissions(
                musicPlayer,
                userQueued
            )
        ) {
            actionWithPerms(musicPlayer, currentTrack)
        } else {
            send(
                "$STOP B-baka, I'm not allowed to let you do that!" +
                        if (alternate == null) "" else "\n\n$THINKING Maybe you meant ``$prefix$alternate`` instead?"
            )
        }
    }

    abstract fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack)
}