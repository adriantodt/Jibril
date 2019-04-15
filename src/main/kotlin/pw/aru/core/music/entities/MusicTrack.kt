package pw.aru.core.music.entities

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.music.internal.TrackData

data class MusicTrack(
    val track: AudioTrack,
    val data: TrackData
) : AudioTrack by track