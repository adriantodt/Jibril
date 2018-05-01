package jibril.commands.games.hungergames

import jibril.commands.games.hungergames.HG.buildHg
import jibril.commands.games.hungergames.HG.handleHg
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*
import kotlin.concurrent.thread

object GameManager {
    val games = LinkedHashMap<String, Game>()

    fun isGameRunning(channel: TextChannel) = games.contains(channel.id)

    fun registerGame(channel: TextChannel, game: Game) {
        games[channel.id] = game
    }

    fun newGame(channel: TextChannel, lobby: Lobby) {
        games[channel.id] = Game(
            lobby,
            thread(name = "HungerGames@${channel.name}") {
                try {
                    handleHg(buildHg(lobby), channel)
                } catch (_: InterruptedException) {
                } finally {
                    removeGame(channel)
                    LobbyManager.registerLobby(channel, lobby)
                }
            }
        )
    }

    fun getGame(channel: TextChannel): Game? {
        return games[channel.id]
    }

    fun removeGame(channel: TextChannel): Game? {
        return games.remove(channel.id)
    }
}