package pw.aru.commands.games

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.lobby.Lobby

interface Game {
    val channel: TextChannel
    val isAlive: Boolean
}

interface GameCreator {
    val description: GameDescription

    fun check(textChannel: TextChannel, lobby: Lobby): Boolean

    fun create(textChannel: TextChannel, lobby: Lobby): Game
}

data class GameDescription(
    val name: String,
    val description: String
)