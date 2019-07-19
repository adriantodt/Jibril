package pw.aru.bot.music.entities

import pw.aru.bot.music.events.StopMusicEvent

sealed class MusicStopReason {
    class UserCommand(val source: MusicEventSource) : MusicStopReason()
    class SystemReason(val reason: StopMusicEvent.Reason) : MusicStopReason()
    object LeftAlone : MusicStopReason()
    object QueueEnded : MusicStopReason()
}