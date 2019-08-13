package pw.aru.bot.music.events

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.music.entities.*
import java.util.*

sealed class InputMusicEvent {
    abstract val source: MusicEventSource
}

data class LoadItemEvent(
    override val source: MusicEventSource,
    val id: UUID,
    val itemIdentifier: String,
    val itemSourceType: ItemSource,
    val loadItemMode: LoadItemMode,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent()

data class EnqueueTrackEvent(
    override val source: MusicEventSource,
    val track: AudioTrack,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent()

data class EnqueuePlaylistEvent(
    override val source: MusicEventSource,
    val playlist: AudioPlaylist,
    val trackLoadOptions: TrackLoadOptions
) : InputMusicEvent()

data class ChangeVolumeEvent(override val source: MusicEventSource, val volume: Int) : InputMusicEvent()

data class ChangePauseStateEvent(override val source: MusicEventSource, val state: PauseState?) : InputMusicEvent()

data class ChangeRepeatModeEvent(override val source: MusicEventSource, val mode: RepeatMode?) : InputMusicEvent()

data class ChangeMusicPositionEvent(override val source: MusicEventSource, val position: Long) : InputMusicEvent()

data class ShuffleQueueEvent(override val source: MusicEventSource) : InputMusicEvent()

data class ClearQueueEvent(override val source: MusicEventSource) : InputMusicEvent()

data class RemoveTrackEvent(override val source: MusicEventSource, val range: IntRange) : InputMusicEvent()

data class SkipTrackEvent(override val source: MusicEventSource) : InputMusicEvent()

data class StopMusicEvent(override val source: MusicEventSource, val reason: Reason? = null) : InputMusicEvent() {
    enum class Reason {
        SILENT,
        VOICE_KICK,
        GUILD_KICK,
        CHANNEL_DELETED,
        MUSIC_SELECTION_CANCELLED,
        BOT_SHUTTING_DOWN
    }
}

data class ToggleVoteEvent(override val source: MusicEventSource, val type: VoteType) : InputMusicEvent()

object DiscordListenersLeftEvent : InputMusicEvent() {
    override val source: MusicEventSource = MusicEventSource.MusicSystem
}