package pw.aru.core.music.entities

sealed class MusicStopReason {
    class UserCommand(val source: MusicEventSource) : MusicStopReason()
    object LeftAlone : MusicStopReason()
    object ChannelDeleted : MusicStopReason()
    object QueueEnded : MusicStopReason()
    object SilentQuit : MusicStopReason()
}