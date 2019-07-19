package pw.aru.commands.games.manager

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import pw.aru.utils.AruTaskExecutor.schedule
import pw.aru.utils.text.SAD
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MINUTES

class LobbyManager {
    private val lobbies = LinkedHashMap<Long, Lobby>()
    private val scheduledRemovals = LinkedHashMap<Long, ScheduledFuture<*>>()

    private fun schedule(channel: TextChannel) {
        val id = channel.idAsLong()
        scheduledRemovals.remove(id)?.cancel(false)
        scheduledRemovals[id] = schedule(5, MINUTES) {
            lobbies.remove(id)
            scheduledRemovals.remove(id)
            channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the lobby...")
        }
    }

    fun getOrCreateLobby(channel: TextChannel, member: Member): Lobby {
        val lobby = lobbies.computeIfAbsent(channel.idAsLong()) { Lobby(member) }
        schedule(channel)
        return lobby
    }

    fun hasLobby(channel: TextChannel) = lobbies.contains(channel.idAsLong())

    fun registerLobby(channel: TextChannel, lobby: Lobby) {
        lobbies[channel.idAsLong()] = lobby
        schedule(channel)
    }

    fun getLobby(channel: TextChannel): Lobby? {
        val lobby = lobbies[channel.idAsLong()]
        if (lobby != null) schedule(channel)
        return lobby
    }

    fun removeLobby(channel: TextChannel): Lobby? {
        val lobby = lobbies.remove(channel.idAsLong())
        if (lobby != null) scheduledRemovals.remove(channel.idAsLong())?.cancel(false)
        return lobby
    }
}