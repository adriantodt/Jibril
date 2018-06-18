package pw.aru.commands.games.hungergames

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.games.lobby.LobbyManager
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.commands.HelpFactory.Companion.prefix
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.toSmartString
import pw.aru.utils.extensions.usage
import java.util.*

@Command("hg", "hungergames")
class HungerGamesCmd : ICommand, ICommand.HelpHandler {
    override val category = Categories.GAMES

    override fun CommandContext.call() {
        val args = parseable()

        if (GameManager.isGameRunning(event.channel)) {
            when (args.takeString()) {
                "cancel", "end", "finish" -> finishGame()
                else -> showHelp()
            }
        } else {
            when (args.takeString()) {
                "new" -> newLobby()
                "join" -> joinLobby()
                "leave" -> leaveLobby()
                "addguests", "addguest" -> addGuests(args.takeRemaining())
                "addall" -> addAllGuests()
                "rmguests", "rmguest" -> rmGuests(args.takeRemaining())
                "clearguests" -> clearGuests()
            //  "configs" -> { }
                "start" -> startGame()
                "", "lobby" -> lobby()
                else -> showHelp()
            }
        }
    }

    private fun CommandContext.finishGame() {
        val game = GameManager.getGame(event.channel)

        if (game == null) {
            send("$CONFUSED Uhhh... What?").queue()
            return
        }

        if (event.author.id != game.lobby.adminId) {
            send("$ERROR You're not the admin of this game, silly!").queue()
            return
        }

        game.thread.interrupt()
        send("$SUCCESS Game stopped.").queue()
    }

    private fun CommandContext.newLobby() {
        val lobby = LobbyManager.getOrCreateLobby(event.channel, event.member)
        val created = lobby.adminId == event.author.id

        send(
            if (created) "$SUCCESS **${event.member.effectiveName}** created a new lobby!\n" +
                "Other players can run ``${prefix}hg join`` to join it!\n" +
                "Use  ``${prefix}hg start`` to start the game!"
            else
                "$ERROR S-sorry, but a lobby (created by **${event.guild.getMemberById(lobby.adminId).effectiveName}**) already exists!\n" +
                    "Use ``${prefix}hg join`` to join it!"
        ).queue()
    }

    private fun CommandContext.joinLobby() {
        val lobby = LobbyManager.getLobby(event.channel)
        when {
            lobby == null -> send(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${prefix}hg new`` and create your lobby!"
            ).queue()

            lobby.adminId == event.author.id || lobby.players.contains(event.member) -> send(
                "$ERROR You're already in that lobby, silly!"
            ).queue()

            else -> {
                val member = event.member
                lobby.players.add(member)
                lobby.playerGuests.remove(member)

                send(
                    "$SUCCESS **${event.member.effectiveName}** joined **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }
        }
    }

    private fun CommandContext.leaveLobby() {
        val lobby = LobbyManager.getLobby(event.channel)

        when {
            lobby == null -> send("$ERROR There's no lobby, silly!").queue()

            lobby.adminId == event.author.id -> {
                LobbyManager.removeLobby(event.channel)

                send("$SUCCESS **${event.member.effectiveName}** closed their lobby.").queue()
            }

            lobby.players.contains(event.member) -> {
                lobby.players.remove(event.member)
                send(
                    "$SUCCESS **${event.member.effectiveName}** left **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }

            lobby.playerGuests.contains(event.member) -> {
                lobby.playerGuests.remove(event.member)
                send(
                    "$SUCCESS *Apparently they didn't liked to be used as tributes*. **${event.member.effectiveName}** left **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }

            else -> send("$ERROR You're not in that lobby, silly!").queue()
        }
    }

    private fun CommandContext.addGuests(args: String) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            send("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        if (args.isEmpty()) {
            showHelp()
            return
        }

        val list = args.split(',').map(String::trim).filterNotTo(ArrayList(), String::isEmpty)

        list.remove("@everyone")
        list.remove("@here")

        val members = list.filter { FinderUtil.USER_MENTION.matcher(it).matches() }
            .onEach { list.remove(it) }
            .flatMap { FinderUtil.findMembers(it, event.guild) }

        lobby.playerGuests.addAll(members)
        lobby.guests.addAll(list)

        val display = listOf(members.map { it.effectiveName }, list).flatten().map { "**$it**" }.toSmartString()

        send("$SUCCESS Added $display as guests!").queue()
    }

    private fun CommandContext.addAllGuests() {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            send("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        lobby.playerGuests.addAll(event.guild.members.filterNot(lobby.players::contains))
    }

    private fun CommandContext.rmGuests(args: String) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            send("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        if (args.isEmpty()) {
            showHelp()
            return
        }

        val list = args.split(',').map(String::trim).filterNotTo(ArrayList(), String::isEmpty)

        list.removeIf { it.contains("@everyone") || it.contains("@here") }

        val members = list.filter { FinderUtil.USER_MENTION.matcher(it).matches() }
            .onEach { list.remove(it) }
            .flatMap { FinderUtil.findMembers(it, event.guild) }
            .toMutableList()

        members.retainAll(lobby.playerGuests)
        list.retainAll(lobby.guests)

        lobby.playerGuests.removeAll(members)
        lobby.guests.removeAll(list)

        val display = listOf(members.map { it.effectiveName }, list).flatten().map { "**$it**" }.toSmartString()

        send("$SUCCESS Removed the guests $display!").queue()
    }

    private fun CommandContext.clearGuests() {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            send("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        lobby.playerGuests.clear()
        lobby.guests.clear()
    }

    private fun CommandContext.startGame() {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            send("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        LobbyManager.removeLobby(event.channel)
        GameManager.newGame(event.channel, lobby)
    }

    private fun CommandContext.lobby() {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            send(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${prefix}hg new`` and create your lobby!"
            ).queue()
            return
        }

        sendEmbed {
            baseEmbed(event, "HungerGames | ${event.guild.getMemberById(lobby.adminId).effectiveName}'s Lobby")

            field("Players:", lobby.players.map { "**${it.effectiveName}**" }.sorted().limitedToString())
            field("Guests:", lobby.guests.map { "**$it**" }.sorted().limitedToString())
        }.queue()
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

    override fun onHelp(event: GuildMessageReceivedEvent) {
        if (GameManager.isGameRunning(event.channel)) {
            event.channel.sendMessage(inGameHelp).queue()
        } else {
            event.channel.sendMessage(lobbyHelp.onHelp(event)).queue()
        }
    }

    private val inGameHelp = arrayOf(
        "**HungerGames** - **In-game**",
        //"hg next".usage("Shows next event."),
        //"hg <end/finish>".usage("Ends the game."),
        //"hg cancel".usage("Abruptly ends the game.")
        "hg <end/finish/cancel>".usage("Ends the game.")
    ).joinToString("\n\n")

    private val lobbyHelp = HelpFactory("HungerGames") {
        aliases("hungergames")
        usage("hg new", "Creates a new game lobby.")
        usage("hg join", "Joins a existing lobby.")
        usage("hg addguests", "Adds guests to the lobby. (You must be the lobby's creator)")
        usage("hg rmguests", "Remove guests to the lobby. (You must be the lobby's creator)")
        usage("hg addall", "Add ALL members from the server as guests of the lobby. (You must be the lobby's creator)")
        usage("hg clearguests", "Remove all guests from the loobby. (You must be the lobby's creator)")
        //usage("hg configs", "Setup the lobby configs. (You must be the lobby's creator)")
        usage("hg start", "Starts a new game. (You must be the lobby's creator)")

        /**
        "new" -> newLobby(event)
        "join" -> joinLobby(event)
        "leave" -> leaveLobby(event)
        "addguests", "addguest" -> addGuests(event, args)
        "addall" -> addAllGuests(event)
        "rmguests", "rmguest" -> rmGuests(event, args)
        "clearguests" -> clearGuests(event)
        "start" -> startGame(event)
        null, "", "lobby" -> lobby(event)
         */
    }
}