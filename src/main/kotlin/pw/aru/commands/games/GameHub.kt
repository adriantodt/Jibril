package pw.aru.commands.games

import pw.aru.commands.games.manager.GameManager
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.embed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.withPrefix

@Command("gamehub", "gh")
@UseFullInjector()
class GameHub(private val gameManager: GameManager) : ICommand {
    override val category = Category.GAMES

    private val games: MutableMap<String, GameCreator> = HashMap()
    private val lobbyManager = gameManager.lobbyManager

    override fun CommandContext.call() {
        val args = parseable()

        val option = args.takeString()

        when (option) {
            "list" -> {
            }

            "new" -> newLobby()
            "join" -> joinLobby()
            "leave" -> leaveLobby()
            "start", "play" -> playGame(args.takeRemaining())
            "lobby", "" -> lobby()
            else -> showHelp()
        }
    }

    private fun CommandContext.newLobby() {
        val lobby = lobbyManager.getOrCreateLobby(event.channel, event.member)
        val created = lobby.adminId == event.author.id

        event.channel.sendMessage(
            if (created) "$SUCCESS **${event.member.effectiveName}** created a new lobby!\n" +
                "Other players can run ``${"hg".withPrefix()} join`` to join it!\n" +
                "Use  ``${"hg".withPrefix()} start`` to start the game!"
            else
                "$ERROR S-sorry, but a lobby (created by **${event.guild.getMemberById(lobby.adminId).effectiveName}**) already exists!\n" +
                    "Use ``${"hg".withPrefix()} join`` to join it!"
        ).queue()
    }

    private fun CommandContext.joinLobby() {
        val lobby = lobbyManager.getLobby(event.channel)
        when {
            lobby == null -> event.channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${HelpFactory.prefix}hg new`` and create your lobby!"
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
        gameManager.newGame(event.channel, lobby, creator)
    }

    private fun CommandContext.lobby() {
        val lobby = lobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${HelpFactory.prefix}hg new`` and create your lobby!"
            ).queue()
            return
        }

        event.channel.sendMessage(
            embed {
                baseEmbed(event, "HungerGames | ${event.guild.getMemberById(lobby.adminId).effectiveName}'s Lobby")

                field("Players:", lobby.players.map { "**${it.effectiveName}**" }.sorted().limitedToString())
            }
        ).queue()
    }

    private fun List<String>.limitedToString(): String {
        if (isEmpty()) return "None"
        else {
            val builder = StringBuilder()
            val iterator = listIterator()

            while (iterator.hasNext()) {
                val next = iterator.next()

                if ((builder.length + next.length + 2) < 1000) {
                    builder.append(next)
                    if (iterator.hasNext()) builder.append(", ")
                } else {
                    builder.append("more ").append(size - iterator.nextIndex()).append("...")
                    break
                }
            }

            return builder.toString()
        }
    }

}

