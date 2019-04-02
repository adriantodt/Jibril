package pw.aru.core.music.events

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.music.entities.*
import java.util.*

sealed class InputMusicEvent(val source: MusicEventSource)

class LoadItemEvent(
    source: MusicEventSource,
    val id: UUID,
    val itemIdentifier: String,
    val itemSourceType: ItemSource,
    val loadItemMode: LoadItemMode,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent(source)

class EnqueueTrackEvent(
    source: MusicEventSource,
    val track: AudioTrack,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent(source)

class EnqueuePlaylistEvent(
    source: MusicEventSource,
    val playlist: AudioPlaylist,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent(source)

class ChangeVolumeEvent(source: MusicEventSource, val volume: Int) : InputMusicEvent(source)

class ChangePauseStateEvent(source: MusicEventSource, val state: PauseState?) : InputMusicEvent(source)

class ChangeRepeatModeEvent(source: MusicEventSource, val mode: RepeatMode?) : InputMusicEvent(source)

class ChangeMusicPositionEvent(source: MusicEventSource, val position: Long) : InputMusicEvent(source)

class ShuffleQueueEvent(source: MusicEventSource) : InputMusicEvent(source)

class ClearQueueEvent(source: MusicEventSource) : InputMusicEvent(source)

class RemoveTrackEvent(source: MusicEventSource, val range: IntRange) : InputMusicEvent(source)

class SkipTrackEvent(source: MusicEventSource) : InputMusicEvent(source)

class StopMusicEvent(source: MusicEventSource, val silent: Boolean = false) : InputMusicEvent(source)

class ToggleVoteEvent(source: MusicEventSource, val type: VoteType) : InputMusicEvent(source)

object DiscordListenersLeftEvent : InputMusicEvent(MusicEventSource.MusicSystem)

//output
