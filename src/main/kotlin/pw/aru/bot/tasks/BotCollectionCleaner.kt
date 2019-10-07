package pw.aru.bot.tasks

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.util.Permission
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunAtStartup
import pw.aru.core.logging.DiscordLogger
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.text.BEG
import pw.aru.utils.text.CRY
import kotlin.math.roundToInt

@RunAtStartup
class BotCollectionCleaner(override val kodein: Kodein) : Executable, KodeinAware {
    private val catnip: Catnip by instance()
    private val db: AruDB by instance()

    private val logger = DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK)

    private val leaveMessage = multiline(
        "$CRY _I'm sorry, I'm sorry, I'm sorry!_",
        "I'm running out of server resources and was told to leave servers which had more bots than users.",
        "$BEG If you want to keep me in your server, you can donate (`donate.aru.pw`) and I'll be able to stay here.",
        "You can also join my support server (`support.aru.pw`) if you want to use my commands."
    )

    override fun run() {
        var left = 0
        for (guild in catnip.cache().guilds()) {
            val counts = guild.members().groupingBy { it.user().bot() }
                .eachCount()

            if (counts.getValue(true) >= (counts.getValue(false) * 1.25).roundToInt()) {
                if (GuildSettings(db, guild.idAsLong()).legacyPremium) return

                val suitableChannel = guild.channels()
                    .findAny { it.isText && guild.selfMember().hasPermissions(it, Permission.SEND_MESSAGES) }
                    .asTextChannel()

                suitableChannel.sendMessage(leaveMessage).thenRun { guild.leave() }
                left++
            }
        }

        logger.embed {
            author("Bot Collection Cleaner")
            color(Colors.blurple)

            description("_Left $left guilds._")
        }
    }
}