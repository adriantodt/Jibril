package pw.aru.bot.music.events

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.entities.*
import pw.aru.bot.music.internal.LavaplayerLoadResult
import java.util.*

sealed class OutputMusicEvent {
    abstract val player: MusicPlayer
    abstract val source: MusicEventSource
}

data class LoadResultsEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val id: UUID,
    val results: LavaplayerLoadResult,
    val trackLoadOptions: TrackLoadOptions
) : OutputMusicEvent()

data class TrackQueuedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val track: MusicTrack
) : OutputMusicEvent()

data class PlaylistQueuedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val playlist: AudioPlaylist,
    val trackLoadOptions: TrackLoadOptions
) : OutputMusicEvent()

data class ConnectErrorEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val error: ConnectionErrorType
) : OutputMusicEvent()

data class MusicStartedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource
) : OutputMusicEvent()

data class ChangedVolumeEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val volume: Int
) : OutputMusicEvent()

data class ChangedPauseStateEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val state: PauseState
) : OutputMusicEvent()

data class ChangedRepeatModeEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val mode: RepeatMode
) : OutputMusicEvent()

data class QueueClearedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource
) : OutputMusicEvent()

data class QueueShuffledEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource
) : OutputMusicEvent()

data class TrackGotStuckEvent(
    override val player: MusicPlayer,
    val track: AudioTrack
) : OutputMusicEvent() {
    override val source = MusicEventSource.AndesiteNode
}

data class TrackErroredEvent(
    override val player: MusicPlayer,
    val track: AudioTrack,
    val reason: String
) : OutputMusicEvent() {
    override val source = MusicEventSource.AndesiteNode
}

data class TrackSkippedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource
) : OutputMusicEvent()

data class NextTrackEvent(
    override val player: MusicPlayer,
    val track: AudioTrack
) : OutputMusicEvent() {
    override val source = MusicEventSource.MusicSystem
}

data class MusicEndedEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val reason: MusicStopReason
) : OutputMusicEvent()

data class ChangedVoteEvent(
    override val player: MusicPlayer,
    override val source: MusicEventSource,
    val voteType: VoteType,
    val added: Boolean,
    val votesLeft: Int
) : OutputMusicEvent()

data class ListenersLeftEvent(
    override val player: MusicPlayer,
    val state: ListenersLeftState
) : OutputMusicEvent() {
    override val source = MusicEventSource.MusicSystem
}


data class PlayerInfoEvent(
    override val player: MusicPlayer,
    val timestamp: Long,
    val position: Long,
    val currentTrack: MusicTrack,
    val queue: List<MusicTrack>
) : OutputMusicEvent() {
    override val source = MusicEventSource.AndesiteNode
}
