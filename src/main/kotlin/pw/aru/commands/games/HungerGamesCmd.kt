package pw.aru.commands.games

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.games.hungergames.GameManager
import pw.aru.commands.games.hungergames.LobbyManager
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.SimpleArgsCommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.commands.HelpFactory.Companion.prefix
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import xyz.cuteclouds.utils.args.ArgParser

@Command("hg", "hungergames")
class HungerGamesCmd : SimpleArgsCommand(expectedArgs = 1, rest = true), ICommand.HelpHandler, ICommand.Invisible {
    override val category = Categories.GAMES

    override fun call(event: GuildMessageReceivedEvent, args: Array<String>) {
        if (GameManager.isGameRunning(event.channel)) {
            when (args.firstOrNull()) {
                "cancel", "end", "finish" -> finishGame(event)
                else -> showHelp()
            }
        } else {
            when (args.firstOrNull()?.toLowerCase()) {
                "new" -> newLobby(event)
                "join" -> joinLobby(event)
                "leave" -> leaveLobby(event)
                "addguests", "addguest" -> addGuests(event, args)
                "addall" -> addAllGuests(event)
                "rmguests", "rmguest" -> rmGuests(event, args)
                "clearguests" -> clearGuests(event)
            //  "configs" -> { }
                "start" -> startGame(event)
                null, "", "lobby" -> lobby(event)
                else -> showHelp()
            }
        }
    }

    private fun finishGame(event: GuildMessageReceivedEvent) {
        val game = GameManager.getGame(event.channel)

        if (game == null) {
            event.channel.sendMessage("$CONFUSED Uhhh... What?").queue()
            return
        }

        if (event.author.id != game.lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this game, silly!").queue()
            return
        }

        game.thread.interrupt()
        event.channel.sendMessage("$SUCCESS Game stopped.").queue()
    }

    private fun newLobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getOrCreateLobby(event.channel, event.member)
        val created = lobby.adminId == event.author.id

        event.channel.sendMessage(
            if (created) "$SUCCESS **${event.member.effectiveName}** created a new lobby!\n" +
                "Other players can run ``${prefix}hg join`` to join it!\n" +
                "Use  ``${prefix}hg start`` to start the game!"
            else
                "$ERROR S-sorry, but a lobby (created by **${event.guild.getMemberById(lobby.adminId).effectiveName}**) already exists!\n" +
                    "Use ``${prefix}hg join`` to join it!"
        ).queue()
    }

    private fun joinLobby(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)
        when {
            lobby == null -> event.channel.sendMessage(
                "$ERROR S-sorry, but there's no lobby here!\n" +
                    "Use ``${prefix}hg new`` and create your lobby!"
            ).queue()

            lobby.adminId == event.author.id || lobby.players.contains(event.member) -> event.channel.sendMessage(
                "$ERROR You're already in that lobby, silly!"
            ).queue()

            else -> {
                val member = event.member
                lobby.players.add(member)
                lobby.playerGuests.remove(member)

                event.channel.sendMessage(
                    "$SUCCESS **${event.member.effectiveName}** joined **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
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

            lobby.playerGuests.contains(event.member) -> {
                lobby.playerGuests.remove(event.member)
                event.channel.sendMessage(
                    "$SUCCESS *Apparently they didn't liked to be used as tributes*. **${event.member.effectiveName}** left **${event.guild.getMemberById(lobby.adminId).effectiveName}**'s lobby!"
                ).queue()
            }

            else -> event.channel.sendMessage("$ERROR You're not in that lobby, silly!").queue()
        }
    }

    private fun addGuests(event: GuildMessageReceivedEvent, args: Array<String>) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        val arg = args.getOrNull(1)

        if (arg == null) {
            showHelp()
            return
        }

        val list = ArgParser(arg).parse().mapNotNull { if (it.isText) it.asText() else null }
            .toMutableList()

        list.remove("@everyone")
        list.remove("@here")

        val members = list.filter { FinderUtil.USER_MENTION.matcher(it).matches() }
            .onEach { list.remove(it) }
            .flatMap { FinderUtil.findMembers(it, event.guild) }

        lobby.playerGuests.addAll(members)
        lobby.guests.addAll(list)

        val display = listOf(members.map { it.effectiveName }, list).flatten().map { "**$it**" }.toSmartString()

        event.channel.sendMessage("$SUCCESS Added $display as guests!").queue()
    }

    private fun addAllGuests(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        lobby.playerGuests.addAll(event.guild.members.filterNot(lobby.players::contains))
    }

    private fun rmGuests(event: GuildMessageReceivedEvent, args: Array<String>) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        val arg = args.getOrNull(1)

        if (arg == null) {
            showHelp()
            return
        }

        val list = ArgParser(arg).parse().mapNotNull { if (it.isText) it.asText() else null }
            .toMutableList()

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

        event.channel.sendMessage("$SUCCESS Removed the guests $display!").queue()
    }

    private fun clearGuests(event: GuildMessageReceivedEvent) {
        val lobby = LobbyManager.getLobby(event.channel)

        if (lobby == null) {
            event.channel.sendMessage("$ERROR There's no lobby for me to start a game, silly!").queue()
            return
        }

        if (event.author.id != lobby.adminId) {
            event.channel.sendMessage("$ERROR You're not the admin of this lobby, silly!").queue()
            return
        }

        lobby.playerGuests.clear()
        lobby.guests.clear()
    }

    private fun startGame(event: GuildMessageReceivedEvent) {
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
                    "Use ``${prefix}hg new`` and create your lobby!"
            ).queue()
            return
        }

        event.channel.sendMessage(
            embed {
                baseEmbed(event, "HungerGames | ${event.guild.getMemberById(lobby.adminId).effectiveName}'s Lobby")
                field(
                    "Players:",
                    if (lobby.players.isEmpty()) arrayOf("None") else lobby.players.map { "**${it.effectiveName}**" }.toTypedArray()
                )
                field(
                    "Guests:",
                    if (lobby.guests.isEmpty()) arrayOf("None") else lobby.guests.map { "**$it**" }.toTypedArray()
                )
            }
        ).queue()
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