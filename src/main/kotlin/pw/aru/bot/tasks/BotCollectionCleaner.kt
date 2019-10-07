package pw.aru.bot.tasks

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.util.Permission
import mu.KLogging
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunEvery
import pw.aru.bot.patreon.Patreon
import pw.aru.bot.permissions.PermissionResolver
import pw.aru.core.logging.DiscordLogger
import pw.aru.core.permissions.UserPermissions.BOT_DEVELOPER
import pw.aru.db.AruDB
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.text.BEG
import pw.aru.utils.text.CRY
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@RunEvery(2, 60, TimeUnit.MINUTES)
class BotCollectionCleaner(override val kodein: Kodein) : Executable, KodeinAware {
    companion object : KLogging()

    private val catnip: Catnip by instance()
    private val db: AruDB by instance()
    private val perms: PermissionResolver by instance()

    private val log = DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK)

    private val leaveMessage = multiline(
        "$CRY _I'm sorry, I'm sorry, I'm sorry!_",
        "I'm running out of server resources and was told to leave servers which had more bots than users.",
        "$BEG If you want to keep me in your server, you can donate (`donate.aru.pw`) and I'll be able to stay here.",
        "You can also join my support server (`support.aru.pw`) if you want to use my commands."
    )

    init {
        logger.info("Cleaning up in 2 minutes...")
    }

    override fun run() {
        logger.info("Cleaning up... This might take a while.")
        var left = 0

        fun checkGuildTask(guild: Guild) = Runnable {
            runCatching {
                val counts = guild.members().groupingBy { it.user().bot() }
                    .eachCount()

                if (counts.getValue(true) >= (counts.getValue(false) * 1.25).roundToInt()) {
                    if (Patreon.isPremium(db, guild.owner()) || BOT_DEVELOPER in perms.resolve(guild.owner()))
                        return@Runnable

                    val suitableChannel = guild.channels()
                        .findAny { it.isText && guild.selfMember().hasPermissions(it, Permission.SEND_MESSAGES) }
                        ?.asTextChannel()

                    suitableChannel?.sendMessage(leaveMessage)?.thenRun { guild.leave() } ?: guild.leave()
                    left++
                }
            }
        }

        val threadPool = Executors.newFixedThreadPool(8)

        val tasks = catnip.cache().guilds().map { threadPool.submit(checkGuildTask(it)) }

        threadPool.shutdown()

        tasks.forEach { it.get() }

        logger.info("Left $left guilds. Will run again in 1 hour.")
        log.embed {
            author("Bot Collection Cleaner")
            color(Colors.blurple)

            description("_Left $left guilds._")
        }
    }

}