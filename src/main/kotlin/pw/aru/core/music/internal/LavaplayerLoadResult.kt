package pw.aru.core.music.internal

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.*

sealed class LavaplayerLoadResult(val id: UUID) {
    class NoMatches(id: UUID) : LavaplayerLoadResult(id)
    class Failed(id: UUID, val exception: FriendlyException) : LavaplayerLoadResult(id)
    class SearchResults(id: UUID, val results: AudioPlaylist) : LavaplayerLoadResult(id)
    class Playlist(id: UUID, val playlist: AudioPlaylist) : LavaplayerLoadResult(id)
    class Track(id: UUID, val track: AudioTrack) : LavaplayerLoadResult(id)
}