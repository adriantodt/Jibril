package pw.aru.commands.games

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.lobby.Lobby
import pw.aru.commands.games.manager.GameManager

interface Game {
    val channel: TextChannel
    val isAlive: Boolean
}

interface GameCreator {
    val description: GameDescription

    fun create(manager: GameManager, channel: TextChannel, lobby: Lobby): Game
}

data class GameDescription(
    val name: String,
    val description: String
)