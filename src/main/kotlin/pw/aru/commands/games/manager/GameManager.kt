package pw.aru.commands.games.manager

import net.dv8tion.jda.core.entities.TextChannel
import org.kodein.di.DKodein
import pw.aru.commands.games.Game
import pw.aru.commands.games.GameCreator
import pw.aru.utils.TaskManager.task
import pw.aru.utils.emotes.BANG
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

class GameManager(val injector: DKodein) {
    val lobbyManager = LobbyManager()

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
        games[channel.id] = creator.create(this, channel, lobby)
        return true
    }

    operator fun get(channel: TextChannel): Game? {
        return games[channel.id]
    }

    fun remove(channel: TextChannel): Game? {
        return games.remove(channel.id)
    }
}