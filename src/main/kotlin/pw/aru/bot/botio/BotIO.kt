package pw.aru.bot.botio

import com.mewna.catnip.Catnip
import mu.KLogging
import org.json.JSONObject
import pw.aru._obsolete.v1.io.AruIO
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunAtStartup
import pw.aru.core.AruSide.AUXILIARY
import pw.aru.utils.AruTaskExecutor.task
import java.util.concurrent.TimeUnit

@RunAtStartup
class BotIO(val io: AruIO, val catnip: Catnip) : Executable {
    companion object : KLogging()

    override fun run() {
        io.configure {
            feed(AUXILIARY, "startup") {
                logger.info("Auxiliary Online, send stats.")
                publishStats()
            }
        }

        logger.info("Sending startup!")
        io.sendFeed(
            "startup",
            JSONObject()
        )

        task(5, TimeUnit.MINUTES) { publishStats() }
    }

    private fun publishStats() {
        io.sendFeed(
            "bot-stats",
            JSONObject()
                .put("user_id", catnip.selfUser()!!.id())
                .put("guild_count", catnip.cache().guilds().size().toString())
        )
    }
}