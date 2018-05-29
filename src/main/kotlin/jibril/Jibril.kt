package jibril

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import jibril.data.config.ConfigManager
import jibril.exported.jibril_version
import jibril.utils.TaskManager.scheduler
import jibril.utils.TaskType
import jibril.utils.humanizedTime
import okhttp3.OkHttpClient
import java.io.File
import java.lang.management.ManagementFactory

object Jibril {
    //Configs
    @Deprecated("Use Injections instead")
    val config = ConfigManager.config

    //Versioning
    const val version = jibril_version

    //Shared Objects
    @Deprecated("Use Injections instead")
    val httpClient: OkHttpClient by lazy { OkHttpClient() }
    @Deprecated("Use Injections instead")
    val eventWaiter: EventWaiter by lazy { EventWaiter(scheduler(TaskType.BUNK), false) }

    //Lists
    val bootQuotes: List<String> get() = File("assets/jibril/boot_quotes.txt").readLines()
    val sleepQuotes: List<String> get() = File("assets/jibril/sleep_quotes.txt").readLines()
    val splashes: List<String> get() = File("assets/jibril/splashes.txt").readLines()

    val uptime: String get() = humanizedTime(rawUptime)
    val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime
}