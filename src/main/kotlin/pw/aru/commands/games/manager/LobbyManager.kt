package pw.aru.commands.games.manager

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.utils.TaskManager.schedule
import pw.aru.utils.emotes.SAD
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MINUTES

class LobbyManager {
    private val lobbies = LinkedHashMap<String, Lobby>()
    private val scheduledRemovals = LinkedHashMap<String, ScheduledFuture<*>>()

    private fun schedule(channel: TextChannel) {
        val id = channel.id
        scheduledRemovals.remove(id)?.cancel(false)
        scheduledRemovals[id] = schedule(5, MINUTES) {
            lobbies.remove(id)
            scheduledRemovals.remove(id)
            channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the lobby...").queue()
        }
    }

    fun getOrCreateLobby(channel: TextChannel, member: Member): Lobby {
        val lobby = lobbies.computeIfAbsent(channel.id) { Lobby(member) }
        schedule(channel)
        return lobby
    }

    fun hasLobby(channel: TextChannel) = lobbies.contains(channel.id)

    fun registerLobby(channel: TextChannel, lobby: Lobby) {
        lobbies[channel.id] = lobby
        schedule(channel)
    }

    fun getLobby(channel: TextChannel): Lobby? {
        val lobby = lobbies[channel.id]
        if (lobby != null) schedule(channel)
        return lobby
    }

    fun removeLobby(channel: TextChannel): Lobby? {
        val lobby = lobbies.remove(channel.id)
        if (lobby != null) scheduledRemovals.remove(channel.id)?.cancel(false)
        return lobby
    }
}