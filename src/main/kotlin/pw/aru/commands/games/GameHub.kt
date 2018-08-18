package pw.aru.commands.games

import pw.aru.commands.games.hg.HGCreator
import pw.aru.commands.games.manager.GameManager
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.embed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.limitedToString
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Command("gamehub", "gh")
@UseFullInjector()
class GameHub(private val gameManager: GameManager) : ICommand {
    override val category = Category.GAMES

    private val games: MutableMap<String, GameCreator> = linkedMapOf("hg" to HGCreator())
    private val playCount: MutableMap<GameCreator, AtomicInteger> = WeakHashMap()

    private val lobbyManager = gameManager.lobbyManager

    override fun CommandContext.call() {
        val args = parseable()

        val option = args.takeString()

        when (option) {
            "" -> if (lobbyManager.hasLobby(channel)) lobby() else introduction()
            "list" -> listGames()
            "new" -> newLobby()
            "join" -> joinLobby()
            "leave" -> leaveLobby()
            "start", "play" -> playGame(args.takeRemaining())
            "lobby" -> lobby()
            else -> showHelp()
        }
    }

    private fun CommandContext.introduction() = send(intro.onHelp(event)).queue()

    private val intro = Help(
        CategoryDescription("Aru!GameHub"),
        Description(
            "The **GameHub** is Aru's lobby and game system. Simply create a new lobby, let your friends join, and you're ready to play!"
        ),
        Usage(
            TextUsage("To create a new lobby:"),
            CommandUsage("gamehub new", "Creates a new lobby on the text channel. It'll fail if the channel already has a lobby."),
            TextUsage("Once you create the lobby, you'll be considered the lobby's admin."),
            UsageSeparator,
            TextUsage("To join a lobby:"),
            CommandUsage("gamehub join", "Joins a lobby of this text channel. It'll fail if the channel doesn't has a lobby."),
            UsageSeparator,
            TextUsage("Finding and playing games:"),
            CommandUsage("gamehub list", "List all available games."),
            CommandUsage("gamehub play <game>", "Starts the game you've chosen."),
            TextUsage("You have to be the lobby's admin to be able to start a game.")
        )
    )

    private fun CommandContext.listGames() {
        sendEmbed {
            baseEmbed(event, "Aru!GameHub | Available Games")
            games.forEach { id, game ->
                val description = game.description
                field(
                    description.name,
                    "(Run ``${prefix}gamehub play $id`` to start this game!)",
                    "",
                    "**Description**:",
                    description.description,
                    "",
                    "**Played ${playCount[game]?.get() ?: 0} times since last startup**"
                )
            }
        }.queue()
    }

    private fun CommandContext.lobby() {
        val lobby = lobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${prefix}gamehub new`` and create your lobby!"
            ).queue()
            return
        }

        event.channel.sendMessage(
            embed {
                baseEmbed(event, "Aru!GameHub | ${event.guild.getMemberById(lobby.adminId).effectiveName}'s Lobby")

                field("Players:", lobby.players.map { "**${it.effectiveName}**" }.sorted().limitedToString(1000))
            }
        ).queue()
    }

    private fun CommandContext.newLobby() {
        val lobby = lobbyManager.getOrCreateLobby(event.channel, event.member)
        val created = lobby.adminId == event.author.id

        event.channel.sendMessage(
            if (created) "$SUCCESS **${event.member.effectiveName}** created a new lobby!\n" +
                "Other players can run ``${prefix}gamehub join`` to join it!\n" +
                "Use  ``${prefix}gamehub play <game>`` to start a game!"
            else
                "$ERROR S-sorry, but a lobby (created by **${event.guild.getMemberById(lobby.adminId).effectiveName}**) already exists!\n" +
                    "Use ``${prefix}gamehub join`` to join it!"
        ).queue()
    }

    private fun CommandContext.joinLobby() {
        val lobby = lobbyManager.getLobby(event.channel)
        when {
            lobby == null -> event.channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${prefix}gamehub new`` and create your lobby!"
            ).queue()

            lobby.adminId == event.author.id || lobby.players.contains(event.member) -> event.channel.sendMessage(
                "$ERROR You're already in that lobby, silly!"
            ).queue()

            else -> {
                val member = event.member
                lobby.players.add(member)

                event.channel.sendMessage(
                    "$SUCCESS **${member.effectiveName}** joined **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }
        }
    }

    private fun CommandContext.leaveLobby() {
        val lobby = lobbyManager.getLobby(event.channel)

        when {
            lobby == null -> event.channel.sendMessage("$ERROR There's no lobby, silly!").queue()

            lobby.adminId == event.author.id -> {
                lobbyManager.removeLobby(event.channel)

                event.channel.sendMessage("$SUCCESS **${event.member.effectiveName}** closed their lobby.").queue()
            }

            lobby.players.contains(event.member) -> {
                lobby.players.remove(event.member)
                event.channel.sendMessage(
                    "$SUCCESS **${event.member.effectiveName}** left **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }

            else -> event.channel.sendMessage("$ERROR You're not in that lobby, silly!").queue()
        }
    }

    private fun CommandContext.playGame(args: String) {
        if (args.isEmpty()) return listGames()

        val lobby = lobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        val creator = games[args]

        if (creator == null) {
            event.channel.sendMessage("$ERROR There's no game named ``$args``!").queue()
            return
        }

        lobbyManager.removeLobby(event.channel)
        playCount.getOrPut(creator, ::AtomicInteger).getAndIncrement()
        gameManager.newGame(event.channel, lobby, creator)
    }
}


