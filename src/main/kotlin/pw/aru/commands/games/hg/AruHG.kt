package pw.aru.commands.games.hg

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.kodein.di.generic.instance
import pw.aru.commands.games.Game
import pw.aru.commands.games.manager.GameManager
import pw.aru.commands.games.manager.lobby.Lobby
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.input.AsyncCommandInput
import pw.aru.core.parser.tryTakeDouble
import pw.aru.hungergames.HungerGames
import pw.aru.hungergames.data.SimpleTribute
import pw.aru.hungergames.events.EventFormatter
import pw.aru.hungergames.game.Actions
import pw.aru.hungergames.game.Phase
import pw.aru.hungergames.loader.loadFile
import pw.aru.hungergames.loader.parseHarmfulActions
import pw.aru.hungergames.loader.parseHarmlessActions
import pw.aru.hungergames.phases.*
import pw.aru.utils.Colors
import pw.aru.utils.emotes.BANG
import pw.aru.utils.emotes.HUNGERGAMES
import pw.aru.utils.emotes.SAD
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.concurrent.thread

class AruHG(private val manager: GameManager, override val channel: TextChannel, private val lobby: Lobby) : Game {

    companion object {
        private val actions: Actions by lazy {
            fun harmlessActions(file: String) = parseHarmlessActions(loadFile(File(file)))
            fun harmfulActions(file: String) = parseHarmfulActions(loadFile(File(file)))

            Actions(
                //Bloodbath
                bloodbathHarmless = harmlessActions("assets/hungergames/events/bloodbath_harmless.txt"),
                bloodbathHarmful = harmfulActions("assets/hungergames/events/bloodbath_harmful.txt"),
                //Day
                dayHarmless = harmlessActions("assets/hungergames/events/day_harmless.txt"),
                dayHarmful = harmfulActions("assets/hungergames/events/day_harmful.txt"),
                //Night
                nightHarmless = harmlessActions("assets/hungergames/events/night_harmless.txt"),
                nightHarmful = harmfulActions("assets/hungergames/events/night_harmful.txt"),
                //Feast
                feastHarmless = harmlessActions("assets/hungergames/events/feast_harmless.txt"),
                feastHarmful = harmfulActions("assets/hungergames/events/feast_harmful.txt")
            )
        }

        private val formatter: EventFormatter = EventFormatter {
            "**${it.name}** ``(${if (it.kills == 1) "1 kill" else it.kills.toString() + " kills"})``"
        }
    }

    init {
        showHelp(true)
        startHgLobby()
    }

    override val isAlive = true

    val players = lobby.players
    val playerGuests = LinkedHashSet<Member>()
    val guests = LinkedHashSet<String>()
    val admin = channel.guild.getMemberById(lobby.adminId)
    var threshold = Threshold.DEFAULT.value

    private fun showHelp(newGame: Boolean = true) {
        channel.sendEmbed {
            baseEmbed("$HUNGERGAMES Aru! HungerGames", color = Colors.discordCanary)
            description(
                "${if (newGame) "New game created successfully! " else ""}**${admin.effectiveName}** is the admin of the game.",
                "",
                "**Game Commands:** (without prefix; you must be the admin of the game)",
                "hg cancel".usage("Returns to the GameHub lobby."),
                "hg exit".usage("Closes the HungerGames and GameHub lobbies."),
                "hg lobby".usage("Shows this game's players guests."),
                "hg addguests".usage("Adds guests to the game."),
                "hg rmguests".usage("Remove guests to the game."),
                "hg addall".usage("Add ALL members from the server as guests of the game."),
                "hg clearguests".usage("Remove all guests from the game."),
                "hg threshold <suicidal/fast/default/slow/peaceful/<any decimal from 0 to 1>>".usage("Sets the \"madness\" of the game."),
                "hg start".usage("Starts a new game.")
            )
        }.queue()
    }

    private fun showInGameHelp() {
        channel.sendEmbed {
            baseEmbed("$HUNGERGAMES Aru! HungerGames", color = Colors.discordCanary)
            description(
                "**${admin.effectiveName}** is the admin of the game.",
                "",
                "**Game Commands:** (without prefix; you must be the admin of the game)",
                "hg cancel".usage("Returns to the HungerGames sub-lobby.")
            )
        }.queue()
    }

    enum class Threshold(val value: Double) {
        SUICIDAL(0.1), FAST(0.3), DEFAULT(0.5), SLOW(0.7), PEACEFUL(0.9);

        override fun toString() = "${name.capitalize()} ($value)"
    }

    private fun startHgLobby() {
        object : AsyncCommandInput(manager.injector.instance(), 2, MINUTES, "hg") {
            override fun filter(event: GuildMessageReceivedEvent) = (event.author.id == admin.id && super.filter(event))

            override fun timeout() {
                channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the game...").queue()
                manager.remove(channel)
            }

            override fun CommandContext.onCommand() {
                val args = parseable()
                when (args.takeString()) {
                    "cancel" -> {
                        manager.remove(channel)
                        send("$SUCCESS **${event.member.effectiveName}** closed their lobby.").queue()
                    }
                    "start" -> {
                        startHg()
                    }
                    "exit" -> {
                        manager.remove(channel)
                        manager.lobbyManager.registerLobby(channel, lobby)
                    }

                    "guests", "lobby" -> {
                        sendEmbed {
                            baseEmbed(event, "HungerGames | ${admin.effectiveName}'s Lobby")

                            field("Players:", players.map { "**${it.effectiveName}**" }.sorted().limitedToString(1000))
                            field(
                                "Guests:",
                                listOf(playerGuests.map(Member::getEffectiveName).sorted(), guests.sorted()).flatten().map { "**$it**" }.limitedToString(1000)
                            )
                        }.queue()
                        waitForNextEvent()
                    }
                    "addguests" -> {
                        if (args.isEmpty()) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        val list = args.takeRemaining().split(',').map(String::trim)
                            .filterNotTo(ArrayList()) { it.isBlank() || it.contains("@everyone", true) || it.contains("@here", true) }

                        val members = list.filter { FinderUtil.USER_MENTION.matcher(it).find() }
                            .onEach { list.remove(it) }
                            .flatMap { it.split(" ", "\n", "\r", "\t") }
                            .flatMap { FinderUtil.findMembers(it, event.guild) }
                            .toMutableList()

                        playerGuests.addAll(members)
                        guests.addAll(list)

                        val display = listOf(members.map { it.effectiveName }, list).flatten().map { "**$it**" }.toSmartString()

                        send("$SUCCESS Added $display as guests!").queue()
                        waitForNextEvent()
                    }
                    "rmguests" -> {
                        if (args.isEmpty()) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        val list = args.takeRemaining().split(',').map(String::trim)
                            .filterNotTo(ArrayList()) { it.isBlank() || it.contains("@everyone", true) || it.contains("@here", true) }

                        val members = list.filter { FinderUtil.USER_MENTION.matcher(it).find() }
                            .onEach { list.remove(it) }
                            .flatMap { it.split(" ", "\n", "\r", "\t") }
                            .flatMap { FinderUtil.findMembers(it, event.guild) }
                            .toMutableList()

                        members.retainAll(playerGuests)
                        list.retainAll(guests)

                        playerGuests.removeAll(members)
                        guests.removeAll(list)

                        val display = listOf(members.map { it.effectiveName }, list).flatten().map { "**$it**" }.toSmartString()

                        send("$SUCCESS Removed the guests $display!").queue()
                        waitForNextEvent()
                    }
                    "addall" -> {
                        val newGuests = event.guild.members.filterNot { players.contains(it) || playerGuests.contains(it) }
                        playerGuests.addAll(newGuests)
                        send("$SUCCESS Added ${newGuests.size} ${if (newGuests.size == 1) "guest" else "guests"}!").queue()
                        waitForNextEvent()
                    }
                    "clearguests" -> {
                        val count = playerGuests.size + guests.size
                        playerGuests.clear()
                        guests.clear()
                        send("$SUCCESS Removed $count ${if (count == 1) "guest" else "guests"}!").queue()
                        waitForNextEvent()
                    }
                    "threshold" -> {
                        if (args.isEmpty()) {
                            send(
                                "$SUCCESS **Threshold**: ${Threshold.values().firstOrNull { it.value == threshold } ?: threshold}"
                            ).queue()
                            waitForNextEvent()
                            return
                        }

                        val enumValue = args.matchFirst(
                            Threshold.values().map { it to fun(s: String) = it.name.equals(s, true) }
                        )
                        if (enumValue != null) {
                            threshold = enumValue.value
                            send(
                                "$SUCCESS **Threshold** set to ${Threshold.values().firstOrNull { it.value == threshold } ?: threshold}"
                            ).queue()
                            waitForNextEvent()
                            return
                        }

                        val numberValue = args.tryTakeDouble()
                        if (numberValue != null) {
                            threshold == numberValue
                            send(
                                "$SUCCESS **Threshold** set to ${Threshold.values().firstOrNull { it.value == threshold } ?: threshold}"
                            ).queue()
                            waitForNextEvent()
                            return
                        }

                        showHelp()
                        waitForNextEvent()
                    }
                    else -> {
                        showHelp()
                        waitForNextEvent()
                    }
                }
            }
        }
    }

    private fun CommandContext.startHg() {
        val players = players.toList()
        val playerGuests = playerGuests.toSet()
        val guests = guests.toSet()
        val threshold = threshold

        val hungerGames = HungerGames(
            listOf(
                players.map(::DiscordTribute),
                playerGuests.map(::DiscordGuestTribute),
                guests.map(::SimpleTribute)
            ).flatten(),
            actions,
            threshold
        )

        val gameThread = thread(name = "HungerGames-$channel") {
            try {
                hungerGames.handle(channel)
                send("$BANG Returning to HungerGames sub-lobby. Use `hg cancel` to return to the game lobby.").queue()
            } catch (e: InterruptedException) {
                send("$BANG Game finished by admin. Returning to HungerGames sub-lobby. Use `hg cancel` to return to the game lobby.").queue()
            }
            startHgLobby()
        }

        startGameLobby(gameThread)
    }

    private fun HungerGames.handle(channel: TextChannel) {
        fun send(vararg messages: Any?) {
            channel.sendMessage(messages.joinToString("\n", transform = Any?::toString)).queue()
        }

        fun quickYield() = Thread.sleep(2500)
        fun yield() = Thread.sleep(15000)

        for (e: Phase in newGame()) {
            when (e) {
                is Bloodbath -> {
                    send("=-=- **The Bloodbath** -=-=")
                    quickYield()

                    for (blocks in e.events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Day -> {
                    send("=-=- **Day ${e.number}** -=-=")
                    quickYield()

                    for (blocks in e.events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is FallenTributes -> {
                    val fallenTributes = e.fallenTributes

                    send(
                        "=-=- **Fallen Tributes** -=-=",
                        "${fallenTributes.size} cannon shots can be heard in the distance.",
                        fallenTributes.joinToString("\n") { "X ${it.format(formatter)}" }
                    )
                    yield()
                }
                is Night -> {
                    send("=-=- **Night ${e.number}** -=-=")
                    quickYield()

                    for (blocks in e.events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Feast -> {
                    send("=-=- **Feast (Day ${e.number})** -=-=")
                    quickYield()

                    for (blocks in e.events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Winner -> {
                    val winner = e.winner
                    send(
                        "=-=- **Winner!** -=-=",
                        formatter.format("{0} is the winner!", listOf(winner))
                    )
                    return
                }
                is Draw -> {
                    send(
                        "=-=- **Draw!** -=-=",
                        "Everyone is dead. No winners."
                    )
                    return
                }
            }
        }
    }

    private fun startGameLobby(gameThread: Thread) {
        object : AsyncCommandInput(manager.injector.instance(), 0, MINUTES, "hg") {
            override fun filter(event: GuildMessageReceivedEvent) = (event.author.id == admin.id && super.filter(event))

            override fun timeout() = Unit

            override fun CommandContext.onCommand() {
                if (!gameThread.isAlive) return

                val args = parseable()
                when (args.takeString()) {
                    "cancel" -> {
                        gameThread.interrupt()
                        send("$SUCCESS **${event.member.effectiveName}** closed their lobby.").queue()
                    }
                    else -> {
                        showInGameHelp()
                        waitForNextEvent()
                    }
                }
            }
        }
    }

}