package pw.aru

import pw.aru.data.config.ConfigManager
import pw.aru.exported.aru_version
import pw.aru.utils.humanizedTime
import java.io.File
import java.lang.management.ManagementFactory

object Aru {
    //Configs
    @Deprecated("Use Injections instead")
    val config = ConfigManager.config

    //Versioning
    const val version = aru_version

    //Lists
    val bootQuotes: List<String> get() = File("assets/aru/boot_quotes.txt").readLines()
    val sleepQuotes: List<String> get() = File("assets/aru/sleep_quotes.txt").readLines()
    val splashes: List<String> get() = File("assets/aru/splashes.txt").readLines()

    val uptime: String get() = humanizedTime(rawUptime)
    val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime
}