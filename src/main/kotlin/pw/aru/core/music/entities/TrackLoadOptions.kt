package pw.aru.core.music.entities

data class TrackLoadOptions(
    val enqueueLoadMode: EnqueueLoadMode,
    val shufflePlaylist: Boolean,
    val volume: Int?,
    val repeatMode: RepeatMode?,
    val startTimestamp: Long?,
    val endTimestamp: Long?
) {
    companion object {
        val DEFAULT = TrackLoadOptions(EnqueueLoadMode.DEFAULT, false, null, null, null, null)
    }
}