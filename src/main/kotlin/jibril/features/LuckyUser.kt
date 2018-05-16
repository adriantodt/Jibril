package jibril.features

import jibril.database.entities.UserProfile
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*
import java.util.function.Consumer

class LuckyUser(
    private val user: User,
    private val luck: Double = 0.25,
    private val base: Int = 10,
    private val extra: Int = 10,
    private val show: Boolean = true
) : Consumer<Message> {
    constructor(event: GuildMessageReceivedEvent, luck: Double = 0.25, base: Int = 10, extra: Int = 10, show: Boolean = true) : this(event.author, luck, base, extra, show)

    companion object {
        private val r = Random()
        private const val coinAdd = "coin_plus:414051827823280139"
    }

    override fun accept(message: Message) {
        if (r.nextDouble() < luck) return

        val profile = UserProfile(user.idLong)

        profile.money += base + r.nextInt(extra)

        if (show) message.addReaction(coinAdd).queue()
    }
}
