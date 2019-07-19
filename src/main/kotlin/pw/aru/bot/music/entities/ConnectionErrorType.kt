package pw.aru.bot.music.entities

enum class ConnectionErrorType {
    MEMBER_NOT_CONNECTED,
    BOT_CONNECTED_TO_OTHER_CHANNEL,
    MEMBER_CHANNEL_FULL,
    BOT_CANT_CONNECT,
    BOT_CONNECT_TIMEOUT
}