package pw.aru.commands.games

import pw.aru.commands.games.lobby.Lobby

interface Game {
}

interface GameCreator {
    val description: GameDescription

    fun check(textChannelId: String, lobby: Lobby): Boolean

    fun create(textChannelId: String, lobby: Lobby): Game?
}

data class GameDescription(
    val name: String,
    val description: String
)