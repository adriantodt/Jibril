package pw.aru.commands.games.manager

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Member

class Lobby(admin: Member) {
    val jda: JDA = admin.jda
    val guild: String = admin.guild.id
    var adminId: String = admin.user.id

    val players = LinkedHashSet<Member>()

    init {
        players += admin
    }
}