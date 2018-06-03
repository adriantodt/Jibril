package pw.aru

import pw.aru.utils.humanizedTime
import java.io.File
import java.lang.management.ManagementFactory

object Aru {
    //Lists
    val developers = listOf(
        //AdrianTodt
        "217747278071463937",
        //Niflheim
        "191410544278765568"
    )

    //Prefix
    val prefixes = ArrayList<String>()

    //Assets
    val bootQuotes: List<String> get() = File("assets/aru/boot_quotes.txt").readLines()
    val sleepQuotes: List<String> get() = File("assets/aru/sleep_quotes.txt").readLines()
    val splashes: List<String> get() = File("assets/aru/splashes.txt").readLines()

    val uptime: String get() = humanizedTime(rawUptime)
    val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime
}