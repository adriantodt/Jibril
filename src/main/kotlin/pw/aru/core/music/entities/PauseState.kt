package pw.aru.core.music.entities

enum class PauseState {
    PAUSED,
    RESUMED;

    fun cycleNext(): PauseState {
        val values = values()
        val next = ordinal + 1
        return values[if (next == values.size) 0 else next]
    }
}