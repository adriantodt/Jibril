package jibril

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import jibril.data.config.ConfigManager
import jibril.data.config.address
import jibril.data.db.ManagedDatabase
import jibril.exported.jibril_version
import jibril.utils.TaskManager.scheduler
import jibril.utils.TaskType
import jibril.utils.humanizedTime
import okhttp3.OkHttpClient
import java.io.File
import java.lang.management.ManagementFactory

object Jibril {
    //Configs
    val config = ConfigManager.config

    //Database
    val db by lazy {
        ManagedDatabase(config.database.address)
    }

    //Versioning
    const val version = jibril_version

    //Shared Objects
    val httpClient: OkHttpClient by lazy { OkHttpClient() }
    val eventWaiter: EventWaiter by lazy { EventWaiter(scheduler(TaskType.BUNK), false) }

    //Lists
    val bootQuotes: List<String> get() = File("assets/jibril/boot_quotes.txt").readLines()
    val sleepQuotes: List<String> get() = File("assets/jibril/sleep_quotes.txt").readLines()
    val splashes: List<String> get() = File("assets/jibril/splashes.txt").readLines()
    val uptime: String get() = humanizedTime(ManagementFactory.getRuntimeMXBean().uptime)
}