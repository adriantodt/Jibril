package pw.aru.bot.music.entities

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.music.internal.TrackData

data class MusicTrack(
    val track: AudioTrack,
    val data: TrackData
) : AudioTrack by track