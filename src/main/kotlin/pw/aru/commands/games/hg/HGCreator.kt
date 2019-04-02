package pw.aru.commands.games.hg

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import pw.aru.commands.games.Game
import pw.aru.commands.games.GameCreator
import pw.aru.commands.games.GameDescription
import pw.aru.commands.games.manager.GameManager

class HGCreator : GameCreator {
    override fun create(manager: GameManager, channel: TextChannel, admin: Member, players: List<Member>): Game? {
        return AruHG(manager, channel, admin, players)
    }

    override val description = GameDescription(
        "HungerGames",
        "Let the games begin! Simulate a Hunger Games match, where only one person will win."
    )
}