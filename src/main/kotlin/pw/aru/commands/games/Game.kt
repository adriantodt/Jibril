package pw.aru.commands.games

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import pw.aru.commands.games.manager.GameManager

interface Game {
    val channel: TextChannel
    val isAlive: Boolean
    fun forcestop()
}

interface GameCreator {
    val description: GameDescription

    fun create(manager: GameManager, channel: TextChannel, admin: Member, players: List<Member>): Game?
}

data class GameDescription(
    val name: String,
    val description: String
)