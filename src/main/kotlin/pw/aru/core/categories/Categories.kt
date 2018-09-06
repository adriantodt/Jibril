package pw.aru.core.categories

import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.CommandPermission.*
import pw.aru.core.commands.help.*
import pw.aru.utils.extensions.toSmartString

enum class Category(val categoryName: String, val help: Help? = null, val nsfw: Boolean = false, val permission: CommandPermission = USER) {
    MUSIC("Music Commands",
        Help(
            CategoryDescription("Music Commands", thumbnail = "https://assets.aru.pw/img/category/music.png"),
            Description("**Aru** provides first-class music for your server!\nLet's get started!"),
            Usage(
                CommandUsage("play [youtube/yt] <search term>", "Searches for the video in Youtube and plays it."),
                CommandUsage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and plays it."),
                CommandUsage("play", "+ attachment", "Loads and plays the song from the attachment."),
                CommandUsage("play <song url>", "Loads and plays the song from the URL."),
                TextUsage("(There's also the ${listOf("playnext", "forceplay", "forceplaynext").toSmartString { "`$prefix$it`" }} commands!)"),
                UsageSeparator,
                CommandUsage("queue", "Displays the current queue."),
                CommandUsage("nowplaying", "Displays the current track playing."),
                UsageSeparator,
                CommandUsage("pause", "Pauses the music player."),
                CommandUsage("resume", "Resumes the music player."),
                CommandUsage("shuffle", "Shuffles the queue."),
                TextUsage("(There's also the ${listOf("votepause", "voteresume", "voteshuffle").toSmartString { "`$prefix$it`" }} commands!)")
            )
        )
    ),
    ACTION("Action Commands"),
    SOCIAL("Social Commands"),
    CURRENCY("Currency Commands"),
    GAMES("Games"),
    IMAGE("Image Commands"),
    FUN("Fun Commands"),
    IMAGEBOARD("Imageboard Commands"),
    MODERATION("Moderation Commands", permission = SERVER_ADMIN),
    DEVELOPER("Developer Commands", permission = BOT_DEVELOPER),
    NSFW_ACTION("NSFW Action Commands", nsfw = true),
    NSFW_IMAGE("NSFW Image Commands", nsfw = true),
    INFO("Info Commands"),
    UTILS("Utility Commands"),
    MISC("Misc Commands");

    override fun toString() = "Category(id = $name, name = $categoryName, permission = $permission)"

    companion object {
        @JvmField
        val LIST = values().toList()

        private val EXTRAS = listOf(
            listOf("musics", "audio") to MUSIC,
            listOf("actions") to ACTION,
            listOf("nsfwimage", "nsfw image", "image nsfw", "nsfwimages", "nsfw images", "images nsfw", "nsfwpics", "nsfw pics", "pics nsfw") to NSFW_IMAGE,
            listOf("nsfwactions", "nsfw actions", "actions nsfw", "nsfwaction", "nsfw action", "action nsfw") to NSFW_ACTION,
            listOf("mod") to MODERATION,
            listOf("util") to UTILS
        ).flatMap { (k, v) -> k.map { it to v } }

        @JvmField
        val SEARCH = LIST.map { it.name.toLowerCase() to it }.plus(EXTRAS).toMap()
    }
}