package pw.aru.core.categories

import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.toSmartString
import pw.aru.utils.extensions.withPrefix

sealed class Category(val id: String, val name: String, val permission: CommandPermission = CommandPermission.USER) {

    override fun toString() = "Category(id = $id, name = $name, permission = $permission)"

    class Simple(id: String, name: String, permission: CommandPermission = CommandPermission.USER) : Category(id, name, permission)

    abstract class HelpDialog(id: String, name: String, permission: CommandPermission = CommandPermission.USER) : Category(id, name, permission), ICommand.HelpDialog {
        abstract override fun onHelp(event: GuildMessageReceivedEvent): MessageEmbed
    }

    abstract class HelpHandler(id: String, name: String, permission: CommandPermission = CommandPermission.USER) : Category(id, name, permission), ICommand.HelpHandler {
        abstract override fun onHelp(event: GuildMessageReceivedEvent)
    }

    abstract class HelpDialogProvider(id: String, name: String, permission: CommandPermission = CommandPermission.USER) : Category(id, name, permission), ICommand.HelpDialogProvider {
        abstract override val helpHandler: ICommand.HelpDialog
    }

    abstract class HelpProvider(id: String, name: String, permission: CommandPermission = CommandPermission.USER) : Category(id, name, permission), ICommand.HelpProvider {
        abstract override val helpHandler: ICommand.HelpHandler
    }
}

object Categories {
    @JvmField
    val MUSIC = object : Category.HelpDialogProvider("music", "Music Commands:") {
        override val helpHandler = HelpFactory(name) {
            categoryMode()

            description("**Aru** provides first-class music for your server!\nLet's get started!")

            usage("play [youtube/yt] <search term>", "Searches for the video in Youtube and plays it.")
            usage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and plays it.")
            usage("play", "+ attachment", "Loads and plays the song from the attachment.")
            usage("play <song url>", "Loads and plays the song from the URL.")

            usageNote("(There's also the ${listOf("playnext", "forceplay", "forceplaynext").toSmartString { "`${it.withPrefix()}`" }} commands!)")
            usageSeparator()

            usage("queue", "Displays the current queue.")
            usage("nowplaying", "Displays the current track playing.")

            usageSeparator()

            usage("pause", "Pauses the music player.")
            usage("resume", "Resumes the music player.")
            usage("shuffle", "Shuffles the queue.")

            usageNote("(There's also the ${listOf("votepause", "voteresume", "voteshuffle").toSmartString { "`${it.withPrefix()}`" }} commands!)")

            val ignored = arrayOf(
                "play", "playnext", "forceplay", "forceplaynext",
                "queue", "nowplaying",
                "pause", "resume", "shuffle",
                "votepause", "voteresume", "voteshuffle"
            )
            seeAlso(
                *CommandRegistry.lookup
                    .mapNotNull { (k, v) ->
                        if (k.category?.id != "music" || k is ICommand.Invisible || ignored.contains(v[0])) null else v[0]
                    }
                    .sorted()
                    .toTypedArray()
            )
        }
    }

    @JvmField
    val ACTION = Category.Simple("action", "Action Commands:")

    @JvmField
    val CURRENCY = Category.Simple("currency", "Currency Commands:")

    @JvmField
    val GAMES = Category.Simple("games", "Games:")

    @JvmField
    val IMAGE = Category.Simple("image", "Image Commands:")

    @JvmField
    val FUN = Category.Simple("fun", "Fun Commands:")

    @JvmField
    val MODERATION = Category.Simple("moderation", "Moderation Commands:", CommandPermission.SERVER_ADMIN)

    @JvmField
    val DEVELOPER = Category.Simple("dev", "Developer Commands:", CommandPermission.BOT_DEVELOPER)

    @JvmField
    val INFO = Category.Simple("info", "Info Commands:")

    @JvmField
    val UTILS = Category.Simple("utils", "Utility Commands:")

    @JvmField
    val MISC = Category.Simple("misc", "Misc Commands:")

    @JvmField
    val LIST = listOf(MUSIC, ACTION, CURRENCY, GAMES, IMAGE, FUN, MODERATION, DEVELOPER, INFO, UTILS, MISC)

    @JvmField
    val REGISTRY = LIST.map { it.id to it }.toMap()
}