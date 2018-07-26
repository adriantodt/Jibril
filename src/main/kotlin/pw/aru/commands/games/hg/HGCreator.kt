package pw.aru.commands.games.hg

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.Game
import pw.aru.commands.games.GameCreator
import pw.aru.commands.games.GameDescription
import pw.aru.commands.games.manager.GameManager
import pw.aru.commands.games.manager.lobby.Lobby

class HGCreator : GameCreator {
    override val description = GameDescription(
        "HungerGames",
        "Let the games begin! Simulate a Hunger Games match, where only one person will win."
    )

    override fun create(manager: GameManager, channel: TextChannel, lobby: Lobby): Game = AruHG(manager, channel, lobby)
}