package pw.aru.bot.categories

import pw.aru.bot.commands.help.*
import pw.aru.core.permissions.MemberPermissions
import pw.aru.core.permissions.Permission
import pw.aru.core.permissions.UserPermissions
import pw.aru.utils.extensions.lang.toSmartString

enum class Category(
    val categoryName: String,
    val help: Help? = null,
    val nsfw: Boolean = false,
    val permissions: List<Permission> = emptyList()
) {
    MUSIC(
        "Music Commands",
        Help(
            CategoryDescription("Music Commands", thumbnail = "https://assets.aru.pw/img/category/music.png"),
            Description("**Aru** provides first-class music for your server!\nLet's get started!"),
            Usage(
                CommandUsage("play [youtube/yt] <search term>", "Searches for the video in Youtube and plays it."),
                CommandUsage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and plays it."),
                CommandUsage("play", "+ attachment", "Loads and plays the song from the attachment."),
                CommandUsage("play <song url>", "Loads and plays the song from the URL."),
                TextUsage(
                    "(There's also the ${listOf(
                        "playnext",
                        "forceplay",
                        "forceplaynext"
                    ).toSmartString { "`$prefix$it`" }} commands!)"
                ),
                UsageSeparator,
                CommandUsage("queue", "Displays the current queue."),
                CommandUsage("nowplaying", "Displays the current track playing."),
                UsageSeparator,
                CommandUsage("pause", "Pauses the music player."),
                CommandUsage("resume", "Resumes the music player."),
                CommandUsage("shuffle", "Shuffles the queue."),
                TextUsage(
                    "(There's also the ${listOf(
                        "votepause",
                        "voteresume",
                        "voteshuffle"
                    ).toSmartString { "`$prefix$it`" }} commands!)"
                )
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
    MODERATION("Moderation Commands", permissions = listOf(MemberPermissions.ADMIN)),
    DEVELOPER("Developer Commands", permissions = listOf(UserPermissions.BOT_DEVELOPER)),
    NSFW_ACTION("NSFW Action Commands", nsfw = true),
    NSFW_IMAGE("NSFW Image Commands", nsfw = true),
    INFO("Info Commands"),
    UTILS("Utility Commands"),
    MISC("Misc Commands");

    override fun toString() = "Category(id = $name, name = $categoryName, permissions = $permissions)"

    companion object {

        @JvmField
        val LIST = values().toList()

        private val EXTRAS = listOf(
            listOf("musics", "audio") to MUSIC,
            listOf("actions") to ACTION,
            listOf(
                "nsfwimage",
                "nsfw image",
                "image nsfw",
                "nsfwimages",
                "nsfw images",
                "images nsfw",
                "nsfwpics",
                "nsfw pics",
                "pics nsfw"
            ) to NSFW_IMAGE,
            listOf(
                "nsfwactions",
                "nsfw actions",
                "actions nsfw",
                "nsfwaction",
                "nsfw action",
                "action nsfw"
            ) to NSFW_ACTION,
            listOf("mod") to MODERATION,
            listOf("util", "utility", "utilities") to UTILS,
            listOf("image board") to IMAGEBOARD
        ).flatMap { (k, v) -> k.map { it to v } }

        @JvmField
        val SEARCH = LIST.map { it.name.toLowerCase() to it }.plus(EXTRAS).toMap()
    }
}