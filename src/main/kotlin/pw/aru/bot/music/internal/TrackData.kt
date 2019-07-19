package pw.aru.bot.music.internal

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.music.entities.MusicEventSource
import pw.aru.bot.music.entities.TrackLoadOptions
import pw.aru.bot.music.utils.ThumbnailResolver.resolveThumbnail
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