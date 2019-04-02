package pw.aru.core.music.internal

import com.github.samophis.lavaclient.events.*
import com.mewna.catnip.Catnip
import com.mewna.catnip.shard.DiscordEvent
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.entities.MusicEventSource
import pw.aru.core.music.events.*
import pw.aru.lib.eventpipes.EventPipes.newAsyncPipe
import pw.aru.lib.eventpipes.api.EventExecutor
import java.io.Closeable
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractMusicPlayer(musicSystem: MusicSystem, catnip: Catnip) {

    private val pipeExecutor = EventExecutor.upgrade { musicSystem.playerOrderedExecutor.submit(this, it) }

    private val inputPipe = newAsyncPipe<InputMusicEvent>(pipeExecutor)
    private val lavaClientPipe = newAsyncPipe<LavalinkEvent>(pipeExecutor)
    private val outputPipe = newAsyncPipe<OutputMusicEvent>()
    private val closeableRefs = newSetFromMap<Closeable>(ConcurrentHashMap())

    init {
        inputPipe.subscribe(::onInputEvent)
        lavaClientPipe.subscribe(::onLavaClientEvent)
        catnip.on(DiscordEvent.VOICE_STATE_UPDATE) {
            if (it.userIdAsLong() == catnip.selfUser()!!.idAsLong() && it.channelIdAsLong() == 0L) {
                publish(StopMusicEvent(MusicEventSource.MusicSystem))
            }
        }
    }

    private fun onInputEvent(event: InputMusicEvent) {
        when (event) {
            is LoadItemEvent -> onLoadItemEvent(event)
            is EnqueueTrackEvent -> onEnqueueTrackEvent(event)
            is EnqueuePlaylistEvent -> onEnqueuePlaylistEvent(event)
            is ChangeVolumeEvent -> onChangeVolumeEvent(event)
            is ChangePauseStateEvent -> onChangePauseStateEvent(event)
            is ChangeRepeatModeEvent -> onChangeRepeatModeEvent(event)
            is ChangeMusicPositionEvent -> onChangeMusicPositionEvent(event)
            is ShuffleQueueEvent -> onShuffleQueueEvent(event)
            is ClearQueueEvent -> onClearQueueEvent(event)
            is RemoveTrackEvent -> onRemoveTrackEvent(event)
            is SkipTrackEvent -> onSkipTrackEvent(event)
            is StopMusicEvent -> onStopMusicEvent(event)
            is ToggleVoteEvent -> onToggleVoteEvent(event)
            is DiscordListenersLeftEvent -> onDiscordListenersLeftEvent(event)
        }
    }

    protected abstract fun onLoadItemEvent(event: LoadItemEvent)

    protected abstract fun onEnqueueTrackEvent(event: EnqueueTrackEvent)

    protected abstract fun onEnqueuePlaylistEvent(event: EnqueuePlaylistEvent)

    protected abstract fun onChangeVolumeEvent(event: ChangeVolumeEvent)

    protected abstract fun onChangePauseStateEvent(event: ChangePauseStateEvent)

    protected abstract fun onChangeRepeatModeEvent(event: ChangeRepeatModeEvent)

    protected abstract fun onChangeMusicPositionEvent(event: ChangeMusicPositionEvent)

    protected abstract fun onShuffleQueueEvent(event: ShuffleQueueEvent)

    protected abstract fun onClearQueueEvent(event: InputMusicEvent)

    protected abstract fun onRemoveTrackEvent(event: RemoveTrackEvent)

    protected abstract fun onSkipTrackEvent(event: SkipTrackEvent)

    protected abstract fun onStopMusicEvent(event: StopMusicEvent)

    protected abstract fun onToggleVoteEvent(event: ToggleVoteEvent)

    protected abstract fun onDiscordListenersLeftEvent(event: DiscordListenersLeftEvent)

    private fun onLavaClientEvent(event: LavalinkEvent) {
        when (event) {
            is TrackStartEvent -> onTrackStartEvent(event)
            is PlayerUpdateEvent -> onPlayerUpdateEvent(event)
            is TrackStuckEvent -> onTrackStuckEvent(event)
            is TrackExceptionEvent -> onTrackExceptionEvent(event)
            is TrackEndEvent -> onTrackEndEvent(event)
        }
    }

    protected abstract fun onTrackStartEvent(event: TrackStartEvent)

    protected abstract fun onPlayerUpdateEvent(event: PlayerUpdateEvent)

    protected abstract fun onTrackStuckEvent(event: TrackStuckEvent)

    protected abstract fun onTrackExceptionEvent(event: TrackExceptionEvent)

    protected abstract fun onTrackEndEvent(event: TrackEndEvent)

    protected fun internalClose() {
        closeableRefs.forEach(Closeable::close)
        closeableRefs.clear()
    }

    protected fun publish(event: OutputMusicEvent) {
        outputPipe.publish(event)
    }

    fun subscribe(onEvent: (OutputMusicEvent) -> Unit) {
        closeableRefs.add(outputPipe.subscribe(onEvent))
    }

    fun publish(event: InputMusicEvent) {
        inputPipe.publish(event)
    }
}