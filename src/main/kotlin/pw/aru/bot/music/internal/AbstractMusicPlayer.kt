package pw.aru.bot.music.internal

import com.mewna.catnip.Catnip
import com.mewna.catnip.shard.DiscordEvent
import mu.KLogging
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.entities.MusicEventSource
import pw.aru.bot.music.events.*
import pw.aru.bot.music.events.StopMusicEvent.Reason.CHANNEL_DELETED
import pw.aru.bot.music.events.StopMusicEvent.Reason.VOICE_KICK
import pw.aru.lib.eventpipes.EventPipes.newAsyncPipe
import pw.aru.libs.andeclient.events.AndePlayerEvent
import pw.aru.libs.andeclient.events.player.PlayerUpdateEvent
import pw.aru.libs.andeclient.events.track.TrackEndEvent
import pw.aru.libs.andeclient.events.track.TrackExceptionEvent
import pw.aru.libs.andeclient.events.track.TrackStartEvent
import pw.aru.libs.andeclient.events.track.TrackStuckEvent
import pw.aru.utils.extensions.lib.asCloseable
import pw.aru.utils.extensions.lib.humanUsersCount
import pw.aru.utils.extensions.lib.voiceState
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

abstract class AbstractMusicPlayer(musicSystem: MusicSystem, catnip: Catnip, guildId: Long) {

    companion object : KLogging()

    private val inputPipe = newAsyncPipe<InputMusicEvent>(musicSystem.pipeExecutor)
    private val outputPipe = newAsyncPipe<OutputMusicEvent>()
    private val closeableRefs = CopyOnWriteArrayList<Closeable>()
    private var lastVoiceChannel: Long = 0L

    init {
        closeableRefs += inputPipe.subscribe(::onInputEvent)
        closeableRefs += musicSystem.playerEventPipe.subscribe(guildId, ::onAndePlayerEvent)
        closeableRefs += outputPipe.subscribe { logger.trace { "sent output event $it" } }
        closeableRefs += catnip.on(DiscordEvent.VOICE_STATE_UPDATE) {
            if (it.guildIdAsLong() == guildId) {
                if (it.userIdAsLong() == catnip.selfUser()!!.idAsLong() && it.channelIdAsLong() == 0L) {
                    publish(
                        StopMusicEvent(
                            MusicEventSource.MusicSystem,
                            if (it.guild()!!.voiceChannel(lastVoiceChannel) == null) CHANNEL_DELETED else VOICE_KICK
                        )
                    )
                } else {
                    lastVoiceChannel = it.channelIdAsLong()

                    if (it.guild()!!.selfMember().voiceState().channel()!!.humanUsersCount == 0) {
                        publish(DiscordListenersLeftEvent)
                    }
                }
            }
        }.asCloseable()
    }

    private fun onInputEvent(event: InputMusicEvent) {
        try {
            logger.trace { "received input event $event" }
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
        } catch (e: Exception) {
            logger.error(e) { "error on InputMusicEvent event $event" }
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

    private fun onAndePlayerEvent(event: AndePlayerEvent) {
        try {
            logger.trace { "received andesite event $event" }
            when (event) {
                is TrackStartEvent -> onTrackStartEvent(event)
                is PlayerUpdateEvent -> onPlayerUpdateEvent(event)
                is TrackStuckEvent -> onTrackStuckEvent(event)
                is TrackExceptionEvent -> onTrackExceptionEvent(event)
                is TrackEndEvent -> onTrackEndEvent(event)
            }
        } catch (e: Exception) {
            logger.error(e) { "error on AndePlayer event $event" }
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