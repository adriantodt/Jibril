package pw.aru.commands.games.hungergames

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.utils.emotes.SAD
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object LobbyManager {
    val executor by lazy { Executors.newSingleThreadScheduledExecutor()!! }
    val lobbys = LinkedHashMap<String, Lobby>()
    val futures = LinkedHashMap<String, ScheduledFuture<*>>()

    private fun schedule(channel: TextChannel) {
        val id = channel.id
        futures.remove(id)?.cancel(false)
        futures[id] = executor.schedule(
            {
                lobbys.remove(id)
                futures.remove(id)
                channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the lobby...").queue()
            },
            5, TimeUnit.MINUTES
        )
    }

    fun getOrCreateLobby(channel: TextChannel, member: Member): Lobby {
        val id = channel.id
        val lobby = lobbys.computeIfAbsent(id) { Lobby(member) }
        schedule(channel)

        return lobby
    }

    fun lobbyExists(channel: TextChannel) = lobbys.contains(channel.id)

    fun registerLobby(channel: TextChannel, lobby: Lobby) {
        val id = channel.id
        lobbys[id] = lobby
        schedule(channel)
    }

    fun getLobby(channel: TextChannel): Lobby? {
        val id = channel.id
        val lobby = lobbys[id]
        if (lobby != null) {
            schedule(channel)
        }

        return lobby
    }

    fun removeLobby(channel: TextChannel): Lobby? {
        val id = channel.id
        val lobby = lobbys.remove(id)
        if (lobby != null) {
            futures.remove(id)?.cancel(false)
        }

        return lobby
    }
}