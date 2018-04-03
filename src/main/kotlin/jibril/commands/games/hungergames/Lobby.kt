package jibril.commands.games.hungergames

import net.dv8tion.jda.core.entities.Member

class Lobby(admin: Member) {
    var adminId = admin.user.id!!

    val players = LinkedHashSet<Member>()
    val playerGuests = LinkedHashSet<Member>()
    val guests = LinkedHashSet<String>()
    var threshold = 0.9

    init {
        players += admin
    }
}