package pw.aru.commands.games.hg

import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.kodein.di.generic.instance
import pw.aru.commands.games.Game
import pw.aru.commands.games.hungergames.HG
import pw.aru.commands.games.manager.GameManager
import pw.aru.commands.games.manager.lobby.Lobby
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.input.AsyncCommandInput
import pw.aru.utils.Colors
import pw.aru.utils.emotes.HUNGERGAMES
import pw.aru.utils.emotes.SAD
import pw.aru.utils.extensions.*
import java.util.concurrent.TimeUnit.MINUTES

class AruHG(manager: GameManager, override val channel: TextChannel, lobby: Lobby) : Game {

    init {
        val players = lobby.players
        val playerGuests = LinkedHashSet<Member>()
        val guests = LinkedHashSet<String>()
        val admin = channel.guild.getMemberById(lobby.adminId)
        var threshold = 0.9

        fun showHelp(boot: Boolean = true) {
            channel.sendEmbed {
                baseEmbed("$HUNGERGAMES Aru! HungerGames", color = Colors.discordCanary)
                description(
                    "${if (boot) "New game created successfully! " else ""}**${admin.effectiveName}** is the admin of the game.",
                    "",
                    "**Game Commands:** (without prefix; you must be the admin of the game)",
                    "hg cancel".usage("Returns to the lobby."),
                    "hg guests".usage("Shows this game's guests."),
                    "hg addguests".usage("Adds guests to the game."),
                    "hg rmguests".usage("Remove guests to the game."),
                    "hg addall".usage("Add ALL members from the server as guests of the game."),
                    "hg clearguests".usage("Remove all guests from the game."),
                    "hg threshold <suicidal/fast/default/slow/peaceful/<any number from 0 to 1>>".usage("Sets the \"madness\" of the game."),
                    "hg start".usage("Starts a new game.")
                )
            }.queue()
        }

        HG.buildHg(players, playerGuests, guests, threshold)

        showHelp(true)

        object : AsyncCommandInput(manager.injector.instance(), 2, MINUTES) {
            override fun filter(event: GuildMessageReceivedEvent) = (event.author.id == lobby.adminId)

            override fun timeout() {
                channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the game...").queue()
                manager.remove(channel)
            }

            override fun CommandContext.onCommand(command: String) {
                if (command != "hg".withPrefix()) return waitForNextEvent()

                val args = parseable()
                when (args.takeString()) {
                    "cancel" -> {
                    }
                    "guests" -> {
                    }
                    "addguests" -> {
                    }
                    "rmguests" -> {
                    }
                    "addall" -> {
                    }
                    "clearguests" -> {
                    }
                    "threshold" -> {
                    }
                    "start" -> {
                    }
                    else -> showHelp()
                }
            }
        }
    }

    override val isAlive = true

}
