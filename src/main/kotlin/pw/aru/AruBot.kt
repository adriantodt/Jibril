package pw.aru

import pw.aru.utils.humanizedTime
import java.lang.management.ManagementFactory

object AruBot {

    //Global aru "definition"s
    val aru get() = myAru

    //Uptime
    val uptime get() = humanizedTime(rawUptime)
    val rawUptime get() = ManagementFactory.getRuntimeMXBean().uptime

    //Prefix
    val prefixes get() = aru.prefixes

    //Assets
    val devs = listOf(
        //AdrianTodt
        "217747278071463937"
    )

    val sleepQuotes = listOf(
        "*goes to sleep*",
        "*hugs pillow*",
        "*hugs blanket*",
        "*hugs Sora*",
        "*hugs Shiro*"
    )

    val splashes = listOf(
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

    val errorQuotes = listOf(
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

    //internal stuff
    internal lateinit var myAru: Aru
}