package pw.aru.commands.games

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.commands.games.hg.HGCreator
import pw.aru.commands.games.manager.GameManager
import pw.aru.core.permissions.MemberPermissions
import pw.aru.utils.extensions.discordapp.safeUserInput
import pw.aru.utils.extensions.lang.limitedToString
import pw.aru.utils.extensions.lib.embed
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.SUCCESS
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Command("gamehub", "gh")
class GameHub(private val gameManager: GameManager) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.GAMES

    private val games: MutableMap<String, GameCreator> = linkedMapOf("hg" to HGCreator())
    private val playCount: MutableMap<GameCreator, AtomicInteger> = WeakHashMap()

    private val lobbyManager = gameManager.lobbyManager

    override fun CommandContext.call() {
        val args = parseable()

        val option = args.takeString()

        if (gameManager.isGameRegistered(channel)) {
            when (option) {
                "forcestop" -> {
                    gameManager.remove(channel)?.forcestop()
                    send("$SUCCESS Game forcibly stopped.")
                }
                else -> {
                    val isAdmin = permissions.contains(MemberPermissions.ADMIN)

                    send(
                        if (isAdmin)
                            "$ERROR There's already a game running! If you need to forcibly stop it, use ``${prefix}gh forcestop``."
                        else
                            "$ERROR There's already a game running! Please, wait for the current game to end or use other TextChannel."
                    )
                }
            }
        } else {
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
    }

    private fun CommandContext.introduction() = send(intro.onHelp(message))

    private val intro = Help(
        CategoryDescription("Aru!GameHub"),
        Description(
            "The **GameHub** is Aru's lobby and game system. Simply create a new lobby, let your friends join, and you're ready to play!"
        ),
        Usage(
            TextUsage("To create a new lobby:"),
            CommandUsage(
                "gamehub new",
                "Creates a new lobby on the text channel. It'll fail if the channel already has a lobby."
            ),
            TextUsage("Once you create the lobby, you'll be considered the lobby's admin."),
            UsageSeparator,
            TextUsage("To join a lobby:"),
            CommandUsage(
                "gamehub join",
                "Joins a lobby of this text channel. It'll fail if the channel doesn't has a lobby."
            ),
            UsageSeparator,
            TextUsage("Finding and playing games:"),
            CommandUsage("gamehub list", "List all available games."),
            CommandUsage("gamehub play <game>", "Starts the game you've chosen."),
            TextUsage("You have to be the lobby's admin to be able to start a game.")
        )
    )

    private fun CommandContext.listGames() {
        sendEmbed {
            styling(message).author("Aru!GameHub | Available Games").applyAll()
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
        }
    }

    private fun CommandContext.lobby() {
        val lobby = lobbyManager.getLobby(channel)

        if (lobby == null) {
            channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                        "Use ``${prefix}gamehub new`` and create your lobby!"
            )
            return
        }

        channel.sendMessage(
            embed {
                styling(message).author("Aru!GameHub | ${lobby.admin().effectiveName()}'s Lobby")

                field(
                    "Players:",
                    lobby.players().asSequence().map { "**${it.effectiveName().safeUserInput()}**" }.sorted().toList().limitedToString(
                        1000
                    )
                )
            }
        )
    }

    private fun CommandContext.newLobby() {
        val lobby = lobbyManager.getOrCreateLobby(channel, author)
        val created = lobby.adminId == author.idAsLong()

        channel.sendMessage(
            if (created) "$SUCCESS **${author.effectiveName().safeUserInput()}** created a new lobby!\n" +
                    "Other players can run ``${prefix}gamehub join`` to join it!\n" +
                    "Use  ``${prefix}gamehub play <game>`` to start a game!"
            else
                "$ERROR S-sorry, but a lobby (created by **${lobby.admin().effectiveName().safeUserInput()}**) already exists!\n" +
                        "Use ``${prefix}gamehub join`` to join it!"
        )
    }

    private fun CommandContext.joinLobby() {
        val lobby = lobbyManager.getLobby(channel)
        when {
            lobby == null -> channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                        "Use ``${prefix}gamehub new`` and create your lobby!"
            )

            lobby.adminId == author.idAsLong() || lobby.players().contains(author) -> channel.sendMessage(
                "$ERROR You're already in that lobby, silly!"
            )

            else -> {
                val member = author
                lobby.addPlayer(member)

                channel.sendMessage(
                    "$SUCCESS **${member.effectiveName().safeUserInput()}** joined **${lobby.admin().effectiveName().safeUserInput()}**'s lobby!"
                )
            }
        }
    }

    private fun CommandContext.leaveLobby() {
        val lobby = lobbyManager.getLobby(channel)

        when {
            lobby == null -> channel.sendMessage("$ERROR There's no lobby, silly!")

            lobby.adminId == author.idAsLong() -> {
                lobbyManager.removeLobby(channel)

                channel.sendMessage("$SUCCESS **${author.effectiveName().safeUserInput()}** closed their lobby.")
            }

            lobby.isPlayer(author) -> {
                lobby.removePlayer(author)
                channel.sendMessage(
                    "$SUCCESS **${author.effectiveName().safeUserInput()}** left **${lobby.admin().effectiveName().safeUserInput()}**'s lobby!"
                )
            }

            else -> channel.sendMessage("$ERROR You're not in that lobby, silly!")
        }
    }

    private fun CommandContext.playGame(args: String) {
        if (args.isEmpty()) return listGames()

        val lobby = lobbyManager.getLobby(channel)

        if (lobby == null) {
            channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!")
            return
        }

        if (author.idAsLong() != lobby.adminId) {
            channel.sendMessage("$ERROR You're not the admin of this lobby, silly!")
            return
        }

        val creator = games[args]

        if (creator == null) {
            channel.sendMessage("$ERROR There's no game named ``$args``!")
            return
        }
        gameManager.newGame(channel, lobby, creator)

        lobbyManager.removeLobby(channel)
        playCount.getOrPut(creator, ::AtomicInteger).getAndIncrement()
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("gamehub", "gh"),
            "Aru!GameHub",
            thumbnail = "https://assets.aru.pw/img/category/games.png"
        ),
        Description(
            "The **GameHub** is Aru's lobby and game system. Simply create a new lobby, let your friends join, and you're ready to play!"
        ),
        Usage(
            CommandUsage("gamehub list", "Lists all available games."),
            UsageSeparator,
            CommandUsage("gamehub new", "Creates a new lobby on this text channel."),
            TextUsage("     (It'll fail if there's already a lobby.)"),
            CommandUsage("gamehub lobby", "Displays this channel's lobby."),
            UsageSeparator,
            CommandUsage("gamehub join", "Joins this channel's lobby."),
            CommandUsage("gamehub leave", "Leaves this channel's lobby."),
            UsageSeparator,
            CommandUsage("gamehub play <game>", "Starts the game you've chosen."),
            TextUsage("     (Only the creator of the lobby can start a game.)")
        )
    )

}


