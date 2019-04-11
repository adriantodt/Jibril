package pw.aru.core.botio

import com.mewna.catnip.Catnip
import org.json.JSONObject
import pw.aru.core.executor.Executable
import pw.aru.core.executor.RunAtStartup
import pw.aru.io.AruIO
import pw.aru.sides.AruSide
import pw.aru.sides.AruSide.*
import pw.aru.utils.AruTaskExecutor.task
import java.util.concurrent.TimeUnit

@RunAtStartup
class BotIO(val io: AruIO, val catnip: Catnip) : Executable {
    override fun run() {
        io.configure {
            feed(AUXILIARY, "startup") { publishStats() }
        }

        io.sendFeed(
            "startup",
            JSONObject()
        )

        task(1, TimeUnit.MINUTES) { publishStats() }
    }

    private fun publishStats() {
        io.sendFeed(
            "bot-stats",
            JSONObject()
                .put("guild_count", catnip.cache().guilds().count())
        )
    }
}