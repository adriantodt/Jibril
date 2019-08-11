package pw.aru.bot.tasks

import com.mewna.catnip.Catnip
import com.mewna.catnip.shard.DiscordEvent
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.CommandProcessor
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunEvery
import pw.aru.core.logging.DiscordLogger
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.lang.format
import pw.aru.utils.extensions.lib.description
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit.DAYS

@RunEvery(1, 1, DAYS)
class BotDailyReport(override val kodein: Kodein) : Executable, KodeinAware {
    private val catnip: Catnip by instance()
    private val processor: CommandProcessor by instance()

    private val logger = DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK)

    private var day = 0
    private var commands = 0
    private var newGuilds = 0

    init {
        catnip.on(DiscordEvent.GUILD_CREATE) { newGuilds++ }
        catnip.on(DiscordEvent.GUILD_DELETE) { newGuilds-- }
    }

    override fun run() {
        day++

        val newCommands = processor.commandCount - commands
        commands += newCommands

        val newGuilds = this.newGuilds
        this.newGuilds = 0

        logger.embed {
            author("Report - Day $day")
            color(AruColors.primary)

            description(
                "**Commands issued**: ${newCommands.format("%,d")}",
                "**New Guilds**: ${newGuilds.format("%,d")}"
            )

            timestamp(OffsetDateTime.now())
        }
    }
}