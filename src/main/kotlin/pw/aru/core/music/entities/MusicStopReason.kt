package pw.aru.core.music.entities

import pw.aru.core.music.events.StopMusicEvent

sealed class MusicStopReason {
    class UserCommand(val source: MusicEventSource) : MusicStopReason()
    class SystemReason(val reason: StopMusicEvent.Reason) : MusicStopReason()
    object LeftAlone : MusicStopReason()
    object QueueEnded : MusicStopReason()
}