package pw.aru

import pw.aru.sides.AruSide
import java.io.File

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
        prefixes = listOf("a!", "aru!", "Aru!"),
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

    companion object {
        fun fromString(type: String): Aru {
            return values().firstOrNull { type.equals(it.name, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid Aru!")
        }
    }
}