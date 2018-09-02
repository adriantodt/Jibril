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
    val bootQuotes get() = File("assets/aru/boot_quotes.txt").readLines()
    val sleepQuotes get() = File("assets/aru/sleep_quotes.txt").readLines()
    val splashes get() = File("assets/aru/splashes.txt").readLines()
    val errorQuotes = listOf(
        "What is happening? I'm sorry, I'm sorry, I'm sorry!",
        "Wha? Everything caught fire! qwq",
        "What am I supposed to do with an error? Because I got one."
    )


    //Uptime
    val uptime get() = humanizedTime(rawUptime)
    val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime
}