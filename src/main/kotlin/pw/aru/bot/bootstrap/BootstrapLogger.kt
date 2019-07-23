package pw.aru.bot.bootstrap

import com.mewna.catnip.util.CatnipMeta
import mu.KLogging
import pw.aru.Aru
import pw.aru.Aru.Bot.aru
import pw.aru.bot.reporting.LocalPastes
import pw.aru.core.exported.aruCore_version
import pw.aru.core.logging.DiscordLogger
import pw.aru.exported.aru_version
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.limit
import pw.aru.utils.extensions.lang.stackTraceToString
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.field
import java.time.OffsetDateTime

class BootstrapLogger : DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK) {
    private companion object : KLogging()

    init {
        text("——————————")
    }

    fun started() {
        logger.info("Booting up...")
        embed {
            author("${aru.botName} - Booting up...")
            color(Colors.discordYellow)

            description(
                "${aru.botName} v$aru_version (aruCore v$aruCore_version) + Catnip ${CatnipMeta.VERSION}",
                "Hol' up, we're starting!"
            )

            timestamp(OffsetDateTime.now())
        }
    }

    fun successful(shardCount: Int, commandCount: Int) {
        logger.info { "Successful boot! $commandCount commands loaded." }
        embed {
            author("${aru.botName} - Successful boot")
            color(Colors.discordGreen)

            description(
                "$shardCount shards loaded.",
                "$commandCount commands loaded."
            )

            timestamp(OffsetDateTime.now())
        }
    }

    fun failed(e: Exception) {
        logger.info("Boot failed.", e)
        embed {
            author("${aru.botName} - Boot failed")
            color(Colors.discordRed)

            field(
                e.javaClass.name,
                e.message!!.limit(1024)
            )
            field("Full Stacktrace:", LocalPastes.paste("Full Stacktrace:", e.stackTraceToString()), "java")

            timestamp(OffsetDateTime.now())
        }
    }

}