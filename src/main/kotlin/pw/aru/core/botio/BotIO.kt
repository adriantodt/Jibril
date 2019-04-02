package pw.aru.core.botio

import pw.aru.core.executor.Executable
import pw.aru.core.executor.RunAtStartup
import pw.aru.io.AruIO

@RunAtStartup
class BotIO(val io: AruIO) : Executable {
    override fun run() {
        io.configure {

        }
    }
}