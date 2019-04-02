package pw.aru.core.music.utils

import com.mewna.catnip.entity.guild.Member
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.entities.MusicEventSource
import pw.aru.core.music.internal.TrackData
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.embed
import pw.aru.utils.extensions.lib.inlineField
import pw.aru.utils.text.PLAY
import pw.aru.utils.text.STREAMING

object NowPlayingEmbed {
    fun nowPlayingEmbed(player: MusicPlayer, memberRequested: Member? = null) = embed {
        val guild = player.guild
        val voiceChannel = player.voiceChannel
        val currentTrack = player.currentTrack!!
        val trackData = player.lastTrackData!!
        val queue = player.queue
        val trackInfo = currentTrack.info

        author("Now Playing:", null, guild.iconUrl())
        color(AruColors.primary)

        if (trackInfo.isStream) {
            description(
                "**[${trackInfo.title}](${trackInfo.uri})** by **${trackInfo.author}**",
                "",
                "$PLAY Streaming... $STREAMING",
                "",
                "**Voice Channel**: ${voiceChannel!!.name()}"
            )
        } else {
            description(
                "**[${trackInfo.title}](${trackInfo.uri})** by **${trackInfo.author}**",
                "",
                "$PLAY ${progressBar(
                    currentTrack.position,
                    currentTrack.duration
                )} (`${musicLength(currentTrack.duration - currentTrack.position)}`)",
                "",
                "**Voice Channel**: ${voiceChannel!!.name()}"
            )
        }

        thumbnail(trackData.thumbnail)

        when (trackData.source) {
            is MusicEventSource.Dashboard -> {
                inlineField(
                    "Requested by:",
                    "**${trackData.source.member(guild)!!.effectiveName()}**, using the Dashboard"
                )
            }
            is MusicEventSource.Discord -> {
                inlineField("Requested by:", "**${trackData.source.member(guild)!!.effectiveName()}**")
            }
        }

        inlineField("Duration:", musicLength(trackInfo.length, "Unknown"))

        if (memberRequested == null) {
            footer(
                "Queue: ${queue.size} songs - ${musicLength(queue)} remaining", guild.iconUrl()
            )
        } else {
            footer(
                "Queue: ${queue.size} songs - ${musicLength(queue)} remaining | Requested by ${memberRequested.effectiveName()}",
                memberRequested.user().effectiveAvatarUrl()
            )
        }

    }

    fun musicLength(millis: Long, stream: String = "stream"): String {
        if (millis == Long.MAX_VALUE) return stream

        val hours = millis / 3600000
        val minutes = millis / 60000 % 60
        val seconds = millis / 1000 % 60

        return if (hours == 0L) "%02d:%02d".format(minutes, seconds) else "%02d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    }

    fun musicLength(queue: Iterable<Pair<AudioTrack, TrackData>>): String {
        val length = queue.asSequence().filterNot { (it) -> it.info.isStream }.map { (it) -> it.duration }.sum()
        val streamCount = queue.count { (it) -> it.info.isStream }

        if (length == 0L && streamCount != 0) {
            return "$streamCount streams"
        }

        if (length != 0L && streamCount == 0) {
            return musicLength(length)
        }

        return "${musicLength(length)} + ${if (streamCount == 1) "1 stream" else "$streamCount streams"}"
    }

    private const val BLOCK_INACTIVE = "\u25AC"
    private const val BLOCK_ACTIVE = "\uD83D\uDD18"
    private const val TOTAL_BLOCKS = 15

    fun progressBar(position: Long, duration: Long): String {
        val active = (position.toDouble() / duration.toDouble()).times(TOTAL_BLOCKS).toInt()
        return (0..TOTAL_BLOCKS).joinToString("") { if (it == active) BLOCK_ACTIVE else BLOCK_INACTIVE }
    }

}