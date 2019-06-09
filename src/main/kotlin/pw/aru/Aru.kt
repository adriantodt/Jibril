package pw.aru

import pw.aru.sides.AruSide
import pw.aru.utils.extensions.lang.environiment
import pw.aru.utils.humanizedTime
import java.io.File
import java.lang.management.ManagementFactory

enum class Aru(
    val side: AruSide,
    val botName: String,
    val environment: String,
    val prefixes: List<String>,
    val pastesRoot: String,
    val reportsRoot: String
) {

    MAIN(
        side = AruSide.MAIN,
        botName = "Aru!",
        environment = "production",
        prefixes = listOf("a!", "aru!", "Aru!", "aru "),
        pastesRoot = "https://pastes.aru.pw",
        reportsRoot = "https://reports.aru.pw"
    ),

    DEV(
        side = AruSide.DEV,
        botName = "AruDev!",
        environment = "development",
        prefixes = listOf("ad!", "arudev ", "arudev!"),
        pastesRoot = File("pastes").absolutePath,
        reportsRoot = File("reports").absolutePath
    ),

    PATREON(
        side = AruSide.PATREON,
        botName = "Aru! Patreon",
        environment = "production",
        prefixes = listOf("ap!", "arupatreon ", "arupatreon!"),
        pastesRoot = "https://pastes-patreonbot.aru.pw",
        reportsRoot = "https://reports-patreonbot.aru.pw"
    );

    object EnvVars {
        val BOT_TYPE by environiment

        val BOT_TOKEN by environiment
        val WEEBSH_TOKEN by environiment

        val REDIS_HOSTNAME by environiment
        val ANDESITE_HOSTNAME by environiment

        val CONSOLE_WEBHOOK by environiment
        val SERVERS_WEBHOOK by environiment
    }

    companion object Bot {
        //Global aru "definition"s
        val aru by lazy {
            values().firstOrNull { EnvVars.BOT_TYPE.equals(it.name, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid Aru!")
        }

        //Uptime
        val uptime get() = humanizedTime(rawUptime)
        val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime

        //Prefix
        val prefixes get() = aru.prefixes

        //Assets
        val devs = arrayOf(
            //AdrianTodt
            "217747278071463937"
        )

        val sleepQuotes = arrayOf(
            "*goes to sleep*",
            "*hugs pillow*",
            "*hugs blanket*",
            "*hugs Sora*",
            "*hugs Shiro*"
        )

        val splashes = arrayOf(
            "Hello everyone!",
            "Hi! I'm Aru!",
            "with Kotlin",
            "now with Catnip",
            "with music",
            "with you",
            "with games",
            "I'm cute!",
            "pat meeee!",
            "Here to help you!",
            "Listen to some music!",
            "I want food!",
            "I'm an angel, you b-baka!",
            "I'm not lewd, you're lewd!",
            "owo",
            "uwu",
            "awau",
            "lewdie!",
            "Whoa.",
            "What's a pancake?",
            "Need support? Check aru!hangout",
            "Discord needs more pink.",
            "Be a Patreon! Check aru!links",
            "Now with a Patreon Bot!"
        )

        val errorQuotes = arrayOf(
            "What is happening? I'm sorry, I'm sorry, I'm sorry!",
            "Wha? Everything caught fire! qwq",
            "What am I supposed to do with an error? Because I got one."
        )

        val evaluatingQuotes = arrayOf(
            "Creating Pylons of Java...",
            "Warming up compilers...",
            "Starting up Reflections...",
            "Building Abstract Syntax Trees...",
            "Recursively interpreting code..."
        )
    }
}