package jibril.features

import jibril.Jibril.db
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*
import java.util.function.Consumer

class LuckyUser(
    private val user: User,
    private val luck: Double = 0.25,
    private val base: Int = 10,
    private val extra: Int = 10
) : Consumer<Message> {
    constructor(event: GuildMessageReceivedEvent, luck: Double = 0.25, base: Int = 10, extra: Int = 10) : this(event.author, luck, base, extra)

    companion object {
        private val r = Random()
        private const val coinAdd = "coin_plus:414051827823280139"
    }

    override fun accept(message: Message) {
        if (r.nextDouble() < luck) return

        val profile = db.userProfiles[user.idLong]
        profile.money += base + r.nextInt(extra)
        profile.save(db)

        message.addReaction(coinAdd).queue()
    }
}
