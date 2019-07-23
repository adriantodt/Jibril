package pw.aru.commands.games.hg

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Message
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.input.AsyncCommandInput
import pw.aru.bot.parser.tryTakeBoolean
import pw.aru.bot.parser.tryTakeDouble
import pw.aru.bot.reporting.ErrorReporter
import pw.aru.commands.games.Game
import pw.aru.commands.games.manager.GameManager
import pw.aru.commands.games.manager.Lobby
import pw.aru.hg.engine.HungerGames
import pw.aru.hg.engine.data.SimpleTribute
import pw.aru.hg.engine.events.EventFormatter
import pw.aru.hg.engine.game.Actions
import pw.aru.hg.engine.game.HarmfulAction
import pw.aru.hg.engine.game.Phase
import pw.aru.hg.engine.game.Tribute
import pw.aru.hg.engine.phases.*
import pw.aru.hg.loader.loadFile
import pw.aru.hg.loader.parseHarmfulActions
import pw.aru.hg.loader.parseHarmlessActions
import pw.aru.libs.catnip.entityfinder.EntityFinder
import pw.aru.utils.Colors
import pw.aru.utils.extensions.discordapp.safeUserInput
import pw.aru.utils.extensions.lang.format
import pw.aru.utils.extensions.lang.limitedToString
import pw.aru.utils.extensions.lang.split
import pw.aru.utils.extensions.lang.toSmartString
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.extensions.lib.inlineField
import pw.aru.utils.extensions.lib.sendEmbed
import pw.aru.utils.styling
import pw.aru.utils.text.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.concurrent.thread

class AruHG(
    private val manager: GameManager,
    override val channel: TextChannel,
    private val admin: Member,
    private val players: List<Member>
) : Game, KodeinAware {
    override val kodein = manager.kodein
    private val catnip by instance<Catnip>()

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

    val playerGuests = LinkedHashSet<Member>()
    val guests = LinkedHashSet<String>()
    var threshold = Threshold.DEFAULT.value
    var speed = Speed.DEFAULT
    var onlyKills = false
    var lastGame: List<Tribute>? = null
    var lastGameDayCount: Int? = null
    var lastGameNightCount: Int? = null
    var lastGameFeastCount: Int? = null
    var lastGameAvgDeaths: Double? = null
    var forceStopped = false

    override val isAlive
        get() = forceStopped

    init {
        showHelp(true)
        startHgLobby()
    }

    override fun forcestop() {
        forceStopped = true
    }

    private fun commandUsage(command: String, description: String) = "`$command` - $description"

    private fun showHelp(newGame: Boolean = true) {
        channel.sendEmbed {
            author("Aru! HungerGames")
            color(Colors.discordYellow)
            thumbnail("https://assets.aru.pw/img/hungergames.png")
            description(
                "${if (newGame) "New game created successfully! " else ""}**${admin.effectiveName().safeUserInput()}** is the admin of the game.",
                "",
                "**HG Lobby Commands:** (without prefix; you must be the admin of the game)",
                commandUsage("hg cancel", "Returns to the GameHub lobby."),
                commandUsage("hg exit", "Closes the HungerGames and GameHub lobbies."),
                "",
                commandUsage("hg lobby", "Shows this game's players guests."),
                commandUsage("hg scoreboard", "Shows last game's scoreboards and stats."),
                "",
                commandUsage("hg addguests", "Adds guests to the game."),
                commandUsage("hg rmguests", "Remove guests to the game."),
                commandUsage("hg addall", "Add ALL members from the server as guests of the game."),
                commandUsage("hg clearguests", "Remove all guests from the game."),
                "",
                commandUsage(
                    "hg threshold <suicidal/fast/default/slow/peaceful/<any decimal from 0 to 1>>",
                    "Sets the \"madness\" of the game."
                ),
                commandUsage("hg speed <insane/fast/default/slow/slowest>", "Sets the speed of the game's events."),
                commandUsage("hg onlykills <true/false>", "Disables harmless actions from appearing on the log."),
                "",
                commandUsage("hg start", "Starts a new game.")
            )
        }
    }

    private fun showInGameHelp() {
        channel.sendEmbed {
            author("Aru! HungerGames")
            color(Colors.discordYellow)
            thumbnail("https://assets.aru.pw/img/hungergames.png")
            description(
                "**${admin.effectiveName().safeUserInput()}** is the admin of the game.",
                "",
                "**Game Commands:** (without prefix; you must be the admin of the game)",
                commandUsage("hg cancel", "Returns to the HungerGames sub-lobby.")
            )
        }
    }

    enum class Threshold(val value: Double) {
        SUICIDAL(0.1), FAST(0.3), DEFAULT(0.5), SLOW(0.7), PEACEFUL(0.9);

        override fun toString() = "${name.toLowerCase().capitalize()} ($value)"
    }

    enum class Speed(val yield: Long, val quickYield: Long) {
        INSANE(1000, 1000), FAST(7500, 1250), DEFAULT(15000, 2500), SLOW(30000, 3000), SLOWEST(45000, 5000);

        override fun toString() = name.toLowerCase().capitalize()
    }

    private fun startHgLobby() {
        object : AsyncCommandInput(catnip, 5, MINUTES, "hg") {
            override fun filter(message: Message) = (message.author().id() == admin.id() && super.filter(message))

            override fun timeout() {
                channel.sendMessage("$SAD Apparently no one wanted to play anymore, so I closed the game...")
                manager.remove(channel)
            }

            override fun CommandContext.onCommand() {
                if (forceStopped) return

                val args = parseable()
                when (args.takeString()) {

                    "cancel" -> {
                        manager.remove(channel)
                        manager.lobbyManager.registerLobby(channel, Lobby(admin).addPlayers(players))
                        send("$SUCCESS **${message.member()!!.effectiveName().safeUserInput()}** closed HG Lobby. Returned to GameHub Lobby.")
                    }
                    "start" -> {
                        startHg()
                    }
                    "exit" -> {
                        manager.remove(channel)
                        send("$SUCCESS **${message.member()!!.effectiveName().safeUserInput()}** closed their lobby.")
                    }

                    "guests", "lobby" -> {
                        sendEmbed {
                            styling(message).author("Aru! HungerGames | ${admin.effectiveName()}'s Lobby").autoFooter()

                            color(Colors.discordYellow)
                            thumbnail("https://assets.aru.pw/img/hungergames.png")

                            field(
                                "Players:",
                                players.asSequence().map { "**${it.effectiveName().safeUserInput()}**" }.sorted().toList().limitedToString(
                                    1000
                                )
                            )
                            field(
                                "Guests:",
                                listOf(
                                    playerGuests.asSequence().map(Member::effectiveName).sorted().asIterable(),
                                    guests.sorted()
                                ).flatten().map { "**$it**" }.limitedToString(1000)
                            )
                        }
                        waitForNextEvent()
                    }
                    "scoreboard" -> {
                        val tributes = lastGame

                        if (tributes == null) {
                            send("$X Sorry, but no games on this lobby were played (and finished). There's nothing to show!")
                            waitForNextEvent()
                            return
                        }

                        sendEmbed {
                            styling(message).author("Aru! HungerGames | Scoreboard of Last Game").autoFooter()
                            color(Colors.discordYellow)
                            thumbnail("https://assets.aru.pw/img/hungergames.png")

                            inlineField(
                                "Last Alive:",
                                tributes.asSequence()
                                    .take(10)
                                    .withIndex()
                                    .joinToString("\n") { (i, it) -> "#${i + 1} - ${it.format(formatter)}" }
                            )

                            inlineField(
                                "Top Killers:",
                                tributes.asSequence()
                                    .filter { it.kills > 0 }
                                    .sortedByDescending { it.kills }
                                    .take(10)
                                    .withIndex()
                                    .joinToString("\n") { (i, it) -> "#${i + 1} - ${it.format(formatter)}" }
                            )

                            field(
                                "Stats:",
                                "**Tributes**: ${tributes.size}",
                                "",
                                "**Days**: $lastGameDayCount",
                                "**Nights**: $lastGameNightCount",
                                "**Feasts**: $lastGameFeastCount",
                                "",
                                "**Cannons shots per day**: ${lastGameAvgDeaths!!.format("%.2f")}"
                            )
                        }
                        waitForNextEvent()
                    }

                    "addguests" -> {
                        if (args.isEmpty()) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        val list = args.takeRemaining()
                            .splitToSequence(',')
                            .map(String::trim)
                            .filterNotTo(ArrayList()) {
                                it.isBlank() || it.contains(
                                    "@everyone",
                                    true
                                ) || it.contains("@here", true)
                            }

                        val members = list.asSequence()
                            .filter { EntityFinder.USER_MENTION.matcher(it).find() }
                            .onEach { list.remove(it) }
                            .flatMap { it.splitToSequence(" ", "\n", "\r", "\t") }
                            .flatMap { EntityFinder.findMembers(it, message.guild()).asSequence() }
                            .toMutableList()

                        playerGuests.addAll(members)
                        guests.addAll(list)

                        val display =
                            listOf(members.map(Member::effectiveName), list).flatten().map { "**$it**" }.toSmartString()

                        send("$SUCCESS Added $display as guests!")
                        waitForNextEvent()
                    }
                    "rmguests" -> {
                        if (args.isEmpty()) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        val list = args.takeRemaining()
                            .splitToSequence(',')
                            .map(String::trim)
                            .filterNotTo(ArrayList()) {
                                it.isBlank() || it.contains(
                                    "@everyone",
                                    true
                                ) || it.contains("@here", true)
                            }

                        val members = list.asSequence()
                            .filter { EntityFinder.USER_MENTION.matcher(it).find() }
                            .onEach { list.remove(it) }
                            .flatMap { it.splitToSequence(" ", "\n", "\r", "\t") }
                            .flatMap { EntityFinder.findMembers(it, message.guild()).asSequence() }
                            .toMutableList()

                        members.retainAll(playerGuests)
                        list.retainAll(guests)

                        playerGuests.removeAll(members)
                        guests.removeAll(list)

                        val display =
                            listOf(members.map(Member::effectiveName), list).flatten().map { "**$it**" }.toSmartString()

                        send("$SUCCESS Removed the guests $display!")
                        waitForNextEvent()
                    }
                    "addall" -> {
                        val newGuests =
                            message.guild()!!.members().filterNot { players.contains(it) || playerGuests.contains(it) }
                        playerGuests.addAll(newGuests)
                        send("$SUCCESS Added ${newGuests.size} ${if (newGuests.size == 1) "guest" else "guests"}!")
                        waitForNextEvent()
                    }
                    "clearguests" -> {
                        val count = playerGuests.size + guests.size
                        playerGuests.clear()
                        guests.clear()
                        send("$SUCCESS Removed $count ${if (count == 1) "guest" else "guests"}!")
                        waitForNextEvent()
                    }
                    "threshold" -> {
                        if (args.isEmpty()) {
                            send(
                                "$SUCCESS **Threshold**: ${Threshold.values().firstOrNull { it.value == threshold }
                                    ?: threshold}"
                            )
                            waitForNextEvent()
                            return
                        }

                        val numberValue = args.tryTakeDouble()
                        if (numberValue != null) {
                            threshold = numberValue
                            send(
                                "$SUCCESS **Threshold** set to ${Threshold.values().firstOrNull { it.value == numberValue }
                                    ?: numberValue}"
                            )
                            waitForNextEvent()
                            return
                        }

                        val enumValue = args.matchFirst(
                            Threshold.values().map { it to fun(s: String) = it.name.equals(s, true) }
                        )

                        if (enumValue != null) {
                            threshold = enumValue.value
                            send(
                                "$SUCCESS **Threshold** set to ${Threshold.values().firstOrNull { it == enumValue }
                                    ?: enumValue}"
                            )
                            waitForNextEvent()
                            return
                        }

                        showHelp()
                        waitForNextEvent()
                    }
                    "speed" -> {
                        if (args.isEmpty()) {
                            send("$SUCCESS **Speed**: $speed")
                            waitForNextEvent()
                            return
                        }

                        val enumValue = args.matchFirst(
                            Speed.values().map { it to fun(s: String) = it.name.equals(s, true) }
                        )
                        if (enumValue == null) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        speed = enumValue
                        send("$SUCCESS **Speed** set to $speed")
                        waitForNextEvent()

                    }
                    "onlykills" -> {
                        if (args.isEmpty()) {
                            send("$SUCCESS **Only Kills**: $onlyKills")
                            waitForNextEvent()
                            return
                        }

                        val value = args.tryTakeBoolean()
                        if (value == null) {
                            showHelp()
                            waitForNextEvent()
                            return
                        }

                        onlyKills = value
                        send("$SUCCESS **Only Kills** set to $onlyKills")
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
        val speed = speed
        val onlyKills = onlyKills

        val hungerGames = HungerGames(
            listOf(
                players.map(::DiscordTribute),
                playerGuests.map(::DiscordGuestTribute),
                guests.map(::SimpleTribute)
            ).flatten(),
            actions,
            threshold
        )

        val gameThread = thread(name = "HungerGames-${channel.id()}") {
            try {
                hungerGames.handle(channel, speed, onlyKills)
                send("$ZAP Returning to HungerGames sub-lobby. Use `hg cancel` to return to the game lobby.")
            } catch (e: InterruptedException) {
                if (!forceStopped) {
                    send("$BANG Game finished by admin. Returning to HungerGames sub-lobby. Use `hg cancel` to return to the game lobby.")
                }
            } catch (e: Exception) {
                ErrorReporter()
                    .logTimestamp()
                    .exception(e)
                    .channel(channel)
                    .extra("game", "HungerGames")
                    .report()
                    .logToFile()
                    .logAsError()
                    .sendErrorMessage()
            }
            startHgLobby()
        }

        startGameLobby(gameThread)
    }

    private fun HungerGames.handle(channel: TextChannel, speed: Speed, onlyKills: Boolean) {
        fun check() {
            if (forceStopped) throw InterruptedException()
        }

        fun send(vararg messages: Any?) {
            check()
            channel.sendMessage(messages.joinToString("\n", transform = Any?::toString))
                .toCompletableFuture().join()
        }

        fun quickYield() {
            check()
            Thread.sleep(speed.quickYield)
            check()
        }

        fun yield() {
            check()
            Thread.sleep(speed.yield)
            check()
        }

        var dayCount = 0
        var nightCount = 0
        var feastCount = 0
        val deathCount = LinkedList<Int>()

        loop@ for (e: Phase in newGame()) {
            when (e) {
                is Bloodbath -> {
                    send("·—·— **The Bloodbath** —·—·")
                    if (onlyKills && e.events.none { it.action is HarmfulAction }) {
                        send("No deaths.")
                        quickYield()
                        continue@loop
                    }
                    quickYield()

                    val events = if (onlyKills) e.events.filter { it.action is HarmfulAction } else e.events
                    for (blocks in events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Day -> {
                    dayCount++
                    send("·—·— **Day ${e.number}** —·—·")
                    if (onlyKills && e.events.none { it.action is HarmfulAction }) {
                        send("No deaths.")
                        quickYield()
                        continue@loop
                    }
                    quickYield()

                    val events = if (onlyKills) e.events.filter { it.action is HarmfulAction } else e.events
                    for (blocks in events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is FallenTributes -> {
                    val fallenTributes = e.fallenTributes
                    deathCount.add(fallenTributes.size)

                    send(
                        "·—·— **Fallen Tributes** —·—·",
                        "${fallenTributes.size} cannon shots can be heard in the distance.",
                        fallenTributes.joinToString("\n") { "X ${it.format(formatter)}" }
                    )
                    yield()
                }
                is Night -> {
                    nightCount++
                    send("·—·— **Night ${e.number}** —·—·")
                    if (onlyKills && e.events.none { it.action is HarmfulAction }) {
                        send("No deaths.")
                        quickYield()
                        continue@loop
                    }
                    quickYield()

                    val events = if (onlyKills) e.events.filter { it.action is HarmfulAction } else e.events
                    for (blocks in events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Feast -> {
                    feastCount++
                    send("·—·— **Feast (Day ${e.number})** —·—·")
                    if (onlyKills && e.events.none { it.action is HarmfulAction }) {
                        send("No deaths.")
                        quickYield()
                        continue@loop
                    }
                    quickYield()

                    val events = if (onlyKills) e.events.filter { it.action is HarmfulAction } else e.events
                    for (blocks in events.split(4, 7)) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Winner -> {
                    val winner = e.winner
                    send(
                        "·—·— **Winner!** —·—·",
                        formatter.format("{0} is the winner!", listOf(winner))
                    )

                    send("·—·— ·—·—·—· —·—·")

                    lastGame = e.ranking
                    lastGameDayCount = dayCount
                    lastGameNightCount = nightCount
                    lastGameFeastCount = feastCount
                    lastGameAvgDeaths = deathCount.average()
                    return
                }
                is Draw -> {
                    send(
                        "·—·— **Draw!** —·—·",
                        "Everyone is dead. No winners."
                    )

                    send("·—·— ·—·—·—· —·—·")

                    lastGame = e.ranking
                    lastGameDayCount = dayCount
                    lastGameNightCount = nightCount
                    lastGameFeastCount = feastCount
                    lastGameAvgDeaths = deathCount.average()
                    return
                }
            }
        }
    }

    private fun startGameLobby(gameThread: Thread) {
        object : AsyncCommandInput(catnip, 30, SECONDS, "hg") {
            override fun filter(message: Message) = (message.author().id() == admin.id() && super.filter(message))

            override fun timeout() {
                if (!gameThread.isAlive) return
                waitForNextEvent()
            }

            override fun CommandContext.onCommand() {
                if (forceStopped) return
                if (!gameThread.isAlive) return

                val args = parseable()
                when (args.takeString()) {
                    "cancel" -> {
                        gameThread.interrupt()
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