package pw.aru.commands.games

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.games.hungergames.GameManager
import pw.aru.commands.games.lobby.LobbyManager
import pw.aru.core.categories.Categories
import pw.aru.core.commands.ArgsCommand
import pw.aru.core.commands.Command
import pw.aru.core.parser.Args
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*

@Command("gamehub", "gh")
class GameHub : ArgsCommand() {
    override val category = Categories.GAMES

    private val games: MutableMap<String, GameCreator> = HashMap()

    override fun call(event: GuildMessageReceivedEvent, args: Args) {
        val option = args.takeString()

        when (option) {
            "list" -> {
            }

            "new" -> newLobby(event)
            "join" -> joinLobby(event)
            "leave" -> leaveLobby(event)
            "start", "play" -> playGame(event, args.takeRemainingStrings())
            "lobby", "" -> lobby(event)
            else -> showHelp()
        }
    }

    private fun newLobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getOrCreateLobby(event.channel, event.member)
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

    private fun joinLobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)
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

    private fun leaveLobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)

        when {
            lobby == null -> event.channel.sendMessage("$ERROR There's no lobby, silly!").queue()

            lobby.adminId == event.author.id -> {
                LobbyManager.removeLobby(event.channel)

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

    private fun playGame(event: GuildMessageReceivedEvent, args: String) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        LobbyManager.removeLobby(event.channel)
        GameManager.newGame(event.channel, lobby)
    }

    private fun lobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)

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

                field("Players:", limitedToString(lobby.players.map { "**${it.effectiveName}**" }.sorted()))
            }
        ).queue()
    }

    private fun limitedToString(it: List<String>): String {
        if (it.isEmpty()) return "None"
        else {
            val builder = StringBuilder()
            val iterator = it.listIterator()

            while (iterator.hasNext()) {
                val next = iterator.next()

                if ((builder.length + next.length + 2) < 1000) {
                    builder.append(next)
                    if (iterator.hasNext()) builder.append(", ")
                } else {
                    builder.append("more ").append(it.size - iterator.nextIndex()).append("...")
                    break
                }
            }

            return builder.toString()
        }
    }

}

