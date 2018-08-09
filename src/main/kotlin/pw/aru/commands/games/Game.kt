package pw.aru.commands.games

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.manager.GameManager
import pw.aru.commands.games.manager.lobby.Lobby

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