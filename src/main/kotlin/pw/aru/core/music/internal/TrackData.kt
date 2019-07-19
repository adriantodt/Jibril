package pw.aru.core.music.internal

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.music.entities.MusicEventSource
import pw.aru.core.music.entities.TrackLoadOptions
import pw.aru.core.music.utils.ThumbnailResolver.resolveThumbnail
import pw.aru.utils.extensions.lang.getValue
import java.util.concurrent.CompletableFuture.supplyAsync

class TrackData(
    val source: MusicEventSource,
    track: AudioTrack,
    val trackLoadOptions: TrackLoadOptions
) {
    val thumbnail: String? by supplyAsync { resolveThumbnail(track) }

    operator fun component1() = source
    operator fun component2() = trackLoadOptions
}