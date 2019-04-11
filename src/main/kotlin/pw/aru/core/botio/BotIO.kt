package pw.aru.core.botio

import com.mewna.catnip.Catnip
import org.json.JSONObject
import pw.aru.core.executor.Executable
import pw.aru.core.executor.RunAtStartup
import pw.aru.io.AruIO

@RunAtStartup
class BotIO(val io: AruIO, val catnip: Catnip) : Executable {
    override fun run() {
        io.configure {

        }

        io.sendFeed(
            "bot-startup",
            JSONObject()
                .put("guild_count", catnip.cache().guilds().count())
        )
    }
}