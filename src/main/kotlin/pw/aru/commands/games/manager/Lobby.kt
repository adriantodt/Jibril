package pw.aru.commands.games.manager

import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member

class Lobby(admin: Member) {
    val catnip = admin.catnip()!!
    val guildId = admin.guild().idAsLong()
    var adminId = admin.user().idAsLong()

    val playerIds = LinkedHashSet<Long>()

    init {
        playerIds += adminId
    }

    fun guild(): Guild {
        return catnip.cache().guild(guildId) ?: throw CleanupLobby(this, true)
    }

    fun admin(): Member {
        return catnip.cache().member(guildId, adminId) ?: throw CleanupLobby(this, false)
    }

    fun admin(member: Member) = apply {
        adminId = member.idAsLong()
    }

    fun players(): List<Member> {
        val memberCache = guild().members()
        val players = playerIds.mapNotNull(memberCache::getById)
        playerIds.retainAll(players.map(Member::idAsLong))
        return players
    }

    fun addPlayer(member: Member) = apply {
        playerIds += member.idAsLong()
    }

    fun removePlayer(member: Member) = apply {
        playerIds.remove(member.idAsLong())
    }

    fun isPlayer(member: Member): Boolean {
        return member.idAsLong() in playerIds
    }

    fun addPlayers(players: Iterable<Member>) = apply {
        playerIds += players.asSequence().map(Member::idAsLong)
    }
}