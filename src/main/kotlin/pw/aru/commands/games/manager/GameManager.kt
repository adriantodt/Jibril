package pw.aru.commands.games.manager

import com.mewna.catnip.entity.channel.TextChannel
import org.kodein.di.DKodein
import pw.aru.commands.games.Game
import pw.aru.commands.games.GameCreator
import pw.aru.utils.AruTaskExecutor.task
import pw.aru.utils.text.BANG
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

class GameManager(val injector: DKodein) {
    val lobbyManager = LobbyManager()

    val games = LinkedHashMap<Long, Game>()

    init {
        task(1, MINUTES) {
            val toRemoval = games.values.filterNot(Game::isAlive)
            if (toRemoval.isNotEmpty()) {
                games.values.removeAll(toRemoval)
                toRemoval.forEach {
                    it.channel.sendMessage("$BANG Game was stuck and got cleaned up.")
                }
            }
        }
    }

    fun isGameRegistered(channel: TextChannel) = games.contains(channel.idAsLong())

    fun registerGame(channel: TextChannel, game: Game) {
        games[channel.idAsLong()] = game
    }

    fun newGame(channel: TextChannel, lobby: Lobby, creator: GameCreator): Boolean {
        if (isGameRegistered(channel)) return false
        games[channel.idAsLong()] = creator.create(this, channel, lobby.admin(), lobby.players()) ?: return false
        return true
    }

    operator fun get(channel: TextChannel): Game? {
        return games[channel.idAsLong()]
    }

    fun remove(channel: TextChannel): Game? {
        return games.remove(channel.idAsLong())
    }
}