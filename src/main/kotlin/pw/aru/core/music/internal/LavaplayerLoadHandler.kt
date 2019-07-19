package pw.aru.core.music.internal

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.*

class LavaplayerLoadHandler(val id: UUID, val resultHandler: (LavaplayerLoadResult) -> Unit) : AudioLoadResultHandler {
    override fun loadFailed(exception: FriendlyException) {
        resultHandler(LavaplayerLoadResult.Failed(id, exception))
    }

    override fun trackLoaded(track: AudioTrack) {
        resultHandler(LavaplayerLoadResult.Track(id, track))
    }

    override fun noMatches() {
        resultHandler(LavaplayerLoadResult.NoMatches(id))
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult) {
            resultHandler(LavaplayerLoadResult.SearchResults(id, playlist))
        } else {
            resultHandler(LavaplayerLoadResult.Playlist(id, playlist))
        }
    }
}

