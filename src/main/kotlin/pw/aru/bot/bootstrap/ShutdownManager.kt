package pw.aru.bot.bootstrap

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.exitProcess

class ShutdownManager {
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    operator fun plusAssign(listener: () -> Unit) {
        listeners += listener
    }

    fun shutdown() {
        listeners.forEach { runCatching(it) }
        exitProcess(0)
    }
}