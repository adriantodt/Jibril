package pw.aru.commands.games.hungergames

import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.features.LuckyUser
import xyz.cuteclouds.hunger.HungerGames
import xyz.cuteclouds.hunger.HungerGamesBuilder
import xyz.cuteclouds.hunger.data.SimpleTribute
import xyz.cuteclouds.hunger.events.EventFormatter
import xyz.cuteclouds.hunger.game.Actions
import xyz.cuteclouds.hunger.game.HarmfulAction
import xyz.cuteclouds.hunger.game.HarmlessAction
import xyz.cuteclouds.hunger.game.Phase
import xyz.cuteclouds.hunger.loader.loadFile
import xyz.cuteclouds.hunger.loader.parseHarmfulActions
import xyz.cuteclouds.hunger.loader.parseHarmlessActions
import xyz.cuteclouds.hunger.phases.*
import java.io.File
import java.util.function.Consumer

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
        fun send(vararg messages: Any?, success: Consumer<Message>? = null) {
            channel.sendMessage(messages.joinToString("\n", transform = Any?::toString)).queue(success)
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
                        formatter.format("{0} is the winner!", listOf(winner)),
                        success = if (winner is DiscordTribute) LuckyUser(winner.member.user, 0.0, 50, 50) else null
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