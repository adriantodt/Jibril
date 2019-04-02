package pw.aru.core.music.entities

enum class RepeatMode {
    NONE,
    QUEUE,
    SONG;

    fun cycleNext(): RepeatMode {
        val values = values()
        val next = ordinal + 1
        return values[if (next == values.size) 0 else next]
    }
}