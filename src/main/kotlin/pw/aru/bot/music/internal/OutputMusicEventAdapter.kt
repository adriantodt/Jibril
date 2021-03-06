package pw.aru.bot.music.internal

import pw.aru.bot.music.events.*

abstract class OutputMusicEventAdapter : (OutputMusicEvent) -> Unit {
    override fun invoke(event: OutputMusicEvent) {
        when (event) {
            is LoadResultsEvent -> onLoadResultsEvent(event)
            is TrackQueuedEvent -> onTrackQueuedEvent(event)
            is PlaylistQueuedEvent -> onPlaylistQueuedEvent(event)
            is ConnectSuccessfulEvent -> onConnectSuccessfulEvent(event)
            is ConnectErrorEvent -> onConnectErrorEvent(event)
            is MusicStartedEvent -> onMusicStartedEvent(event)
            is ChangedVolumeEvent -> onChangedVolumeEvent(event)
            is ChangedPauseStateEvent -> onChangedPauseStateEvent(event)
            is ChangedRepeatModeEvent -> onChangedRepeatModeEvent(event)
            is TrackGotStuckEvent -> onTrackGotStuckEvent(event)
            is TrackErroredEvent -> onTrackErroredEvent(event)
            is TrackSkippedEvent -> onTrackSkippedEvent(event)
            is NextTrackEvent -> onNextTrackEvent(event)
            is MusicEndedEvent -> onMusicEndedEvent(event)
            is ChangedVoteEvent -> onChangedVoteEvent(event)
            is ListenersLeftEvent -> onListenersLeftEvent(event)
            is PlayerInfoEvent -> onPlayerInfoEvent(event)
            is QueueClearedEvent -> onQueueClearedEvent(event)
            is QueueShuffledEvent -> onQueueShuffledEvent(event)
        }
    }

    abstract fun onQueueShuffledEvent(event: QueueShuffledEvent)

    abstract fun onQueueClearedEvent(event: QueueClearedEvent)

    abstract fun onLoadResultsEvent(event: LoadResultsEvent)

    abstract fun onTrackQueuedEvent(event: TrackQueuedEvent)

    abstract fun onPlaylistQueuedEvent(event: PlaylistQueuedEvent)

    abstract fun onConnectSuccessfulEvent(event: ConnectSuccessfulEvent)

    abstract fun onConnectErrorEvent(event: ConnectErrorEvent)

    abstract fun onMusicStartedEvent(event: MusicStartedEvent)

    abstract fun onChangedVolumeEvent(event: ChangedVolumeEvent)

    abstract fun onChangedPauseStateEvent(event: ChangedPauseStateEvent)

    abstract fun onChangedRepeatModeEvent(event: ChangedRepeatModeEvent)

    abstract fun onTrackGotStuckEvent(event: TrackGotStuckEvent)

    abstract fun onTrackErroredEvent(event: TrackErroredEvent)

    abstract fun onTrackSkippedEvent(event: TrackSkippedEvent)

    abstract fun onNextTrackEvent(event: NextTrackEvent)

    abstract fun onMusicEndedEvent(event: MusicEndedEvent)

    abstract fun onChangedVoteEvent(event: ChangedVoteEvent)

    abstract fun onListenersLeftEvent(event: ListenersLeftEvent)

    abstract fun onPlayerInfoEvent(event: PlayerInfoEvent)
}