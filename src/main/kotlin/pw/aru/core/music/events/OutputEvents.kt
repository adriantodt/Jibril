package pw.aru.core.music.events

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.entities.*
import pw.aru.core.music.internal.LavaplayerLoadResult
import java.util.*

sealed class OutputMusicEvent(val player: MusicPlayer, val source: MusicEventSource)

class LoadResultsEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val id: UUID,
    val results: LavaplayerLoadResult,
    val trackLoadOptions: TrackLoadOptions
) : OutputMusicEvent(player, source)

class TrackQueuedEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val track: MusicTrack
) : OutputMusicEvent(player, source)

class PlaylistQueuedEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val playlist: AudioPlaylist,
    val trackLoadOptions: TrackLoadOptions
) : OutputMusicEvent(player, source)

class ConnectErrorEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val error: ConnectionErrorType
) : OutputMusicEvent(player, source)

class MusicStartedEvent(
    player: MusicPlayer,
    source: MusicEventSource
) : OutputMusicEvent(player, source)

class ChangedVolumeEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val volume: Int
) : OutputMusicEvent(player, source)

class ChangedPauseStateEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val state: PauseState
) : OutputMusicEvent(player, source)

class ChangedRepeatModeEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val mode: RepeatMode
) : OutputMusicEvent(player, source)

class QueueClearedEvent(
    player: MusicPlayer,
    source: MusicEventSource
) : OutputMusicEvent(player, source)

class QueueShuffledEvent(
    player: MusicPlayer,
    source: MusicEventSource
) : OutputMusicEvent(player, source)

class TrackGotStuckEvent(
    player: MusicPlayer,
    val track: AudioTrack
) : OutputMusicEvent(player, MusicEventSource.AndesiteNode)

class TrackErroredEvent(
    player: MusicPlayer,
    val track: AudioTrack,
    val reason: String
) : OutputMusicEvent(player, MusicEventSource.AndesiteNode)

class TrackSkippedEvent(
    player: MusicPlayer,
    source: MusicEventSource
) : OutputMusicEvent(player, source)

class NextTrackEvent(
    player: MusicPlayer,
    val track: AudioTrack
) : OutputMusicEvent(player, MusicEventSource.MusicSystem)

class MusicEndedEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val reason: MusicStopReason
) : OutputMusicEvent(player, source)

class ChangedVoteEvent(
    player: MusicPlayer,
    source: MusicEventSource,
    val voteType: VoteType,
    val added: Boolean,
    val votesLeft: Int
) : OutputMusicEvent(player, source)

class ListenersLeftEvent(
    player: MusicPlayer,
    val state: ListenersLeftState
) : OutputMusicEvent(player, MusicEventSource.MusicSystem)

class PlayerInfoEvent(
    player: MusicPlayer,
    val timestamp: Long,
    val position: Long,
    val currentTrack: MusicTrack,
    val queue: List<MusicTrack>
) : OutputMusicEvent(player, MusicEventSource.AndesiteNode)