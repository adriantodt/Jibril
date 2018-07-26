package pw.aru.commands.games.manager

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.Game
import pw.aru.commands.games.GameCreator
import pw.aru.commands.games.lobby.Lobby
import pw.aru.utils.TaskManager.task
import pw.aru.utils.emotes.BANG
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

object GameManager {
    val games = LinkedHashMap<String, Game>()

    init {
        task(1, MINUTES) {
            val toRemoval = games.values.filterNot(Game::isAlive)
            if (toRemoval.isNotEmpty()) {
                games.values.removeAll(toRemoval)
                toRemoval.forEach {
                    it.channel.sendMessage("$BANG Game was stuck and got cleaned up.").queue()
                }
            }
        }
    }

    fun isGameRegistered(channel: TextChannel) = games.contains(channel.id)

    fun registerGame(channel: TextChannel, game: Game) {
        games[channel.id] = game
    }

    fun newGame(channel: TextChannel, lobby: Lobby, creator: GameCreator): Boolean {
        if (isGameRegistered(channel)) return false
        games[channel.id] = creator.create(channel, lobby)
        return true
    }

    operator fun get(channel: TextChannel): Game? {
        return games[channel.id]
    }

    fun remove(channel: TextChannel): Game? {
        return games.remove(channel.id)
    }
}