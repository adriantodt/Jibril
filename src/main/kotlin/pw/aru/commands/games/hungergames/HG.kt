package pw.aru.commands.games.hungergames

import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.commands.games.hg.DiscordGuestTribute
import pw.aru.commands.games.hg.DiscordTribute
import pw.aru.hungergames.HungerGames
import pw.aru.hungergames.HungerGamesBuilder
import pw.aru.hungergames.data.SimpleTribute
import pw.aru.hungergames.events.EventFormatter
import pw.aru.hungergames.game.Actions
import pw.aru.hungergames.game.HarmfulAction
import pw.aru.hungergames.game.HarmlessAction
import pw.aru.hungergames.game.Phase
import pw.aru.hungergames.loader.loadFile
import pw.aru.hungergames.loader.parseHarmfulActions
import pw.aru.hungergames.loader.parseHarmlessActions
import pw.aru.hungergames.phases.*
import java.io.File

object HG {
    val actions: Actions by lazy {
        Actions(
            bloodbathHarmless = harmlessActions("assets/hungergames/events/bloodbath_harmless.txt"),
            bloodbathHarmful = harmfulActions("assets/hungergames/events/bloodbath_harmful.txt"),
            dayHarmless = harmlessActions("assets/hungergames/events/day_harmless.txt"),
            dayHarmful = harmfulActions("assets/hungergames/events/day_harmful.txt"),
            nightHarmless = harmlessActions("assets/hungergames/events/night_harmless.txt"),
            nightHarmful = harmfulActions("assets/hungergames/events/night_harmful.txt"),
            feastHarmless = harmlessActions("assets/hungergames/events/feast_harmless.txt"),
            feastHarmful = harmfulActions("assets/hungergames/events/feast_harmful.txt")
        )
    }

    fun buildHg(lobby: Lobby): HungerGames {
        return HungerGamesBuilder()
            .actions(actions)
            .addTributes(
                listOf(
                    lobby.players.map(::DiscordTribute),
                    lobby.playerGuests.map(::DiscordGuestTribute),
                    lobby.guests.map(::SimpleTribute)
                ).flatten()
            )
            .threshold(lobby.threshold)
            .build()
    }

    private fun harmlessActions(file: String): List<HarmlessAction> = parseHarmlessActions(loadFile(File(file)))
    private fun harmfulActions(file: String): List<HarmfulAction> = parseHarmfulActions(loadFile(File(file)))

    private val formatter: EventFormatter = EventFormatter {
        "**${it.name}** ``(${if (it.kills == 1) "1 kill" else it.kills.toString() + " kills"})``"
    }

    private fun <E> List<E>.split(minSize: Int = 4, maxSize: Int = 7): List<List<E>> {
        if (size < maxSize) return listOf(this)
        val c = (minSize..maxSize).minBy { it - size % it } ?: 5
        return withIndex()
            .groupBy { it.index / c }
            .values
            .map { it.map { it.value } }
    }

    fun handleHg(hungerGames: HungerGames, channel: TextChannel) {
        fun send(vararg messages: Any?) {
            channel.sendMessage(messages.joinToString("\n", transform = Any?::toString)).queue()
        }

        fun quickYield() = Thread.sleep(2500)
        fun yield() = Thread.sleep(15000)

        for (e: Phase in hungerGames.newGame()) {
            when (e) {
                is Bloodbath -> {
                    send("=-=- **The Bloodbath** -=-=")
                    quickYield()

                    for (blocks in e.events.split()) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Day -> {
                    send("=-=- **Day ${e.number}** -=-=")
                    quickYield()

                    for (blocks in e.events.split()) {
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

                    for (blocks in e.events.split()) {
                        send(blocks.joinToString("\n") { it.format(formatter) })
                        yield()
                    }
                }
                is Feast -> {
                    send("=-=- **Feast (Day ${e.number})** -=-=")
                    quickYield()

                    for (blocks in e.events.split()) {
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
}