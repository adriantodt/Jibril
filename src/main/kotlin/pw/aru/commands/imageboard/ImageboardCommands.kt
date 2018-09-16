package pw.aru.commands.imageboard

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.kodehawa.lib.imageboards.DefaultImageBoards.*
import net.kodehawa.lib.imageboards.ImageBoard
import net.kodehawa.lib.imageboards.entities.Rating
import net.kodehawa.lib.imageboards.entities.Rating.*
import net.kodehawa.lib.imageboards.entities.exceptions.QueryFailedException
import pw.aru.commands.imageboard.ImageboardCommand.Type.RANDOM_IMAGE
import pw.aru.commands.imageboard.ImageboardCommand.Type.SEARCH_RESULT
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category.IMAGEBOARD
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommandProvider
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.parser.parseAndCreate
import pw.aru.core.parser.tryTakeInt
import pw.aru.utils.emotes.DISAPPOINTED
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.TALKING
import pw.aru.utils.emotes.WARNING
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.description
import pw.aru.utils.extensions.image
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

@CommandProvider
class ImageboardCommands : ICommandProvider {

    override fun provide(r: CommandRegistry) {
        ImageboardCommand(
            listOf("rule34", "r34"), "Rule34",
            board = RULE34, nsfwOnly = true
        ).register(r)

        ImageboardCommand(
            listOf("e621"), "e621",
            board = E621, nsfwOnly = true
        ).register(r)

        ImageboardCommand(
            listOf("yandere"), "Yande.re",
            board = YANDERE, nsfwOnly = true
        ).register(r)

        ImageboardCommand(
            listOf("konachan"), "Konachan",
            board = KONACHAN
        ).register(r)

        ImageboardCommand(
            listOf("danbooru"), "Danbooru",
            board = DANBOORU
        ).register(r)

        ImageboardCommand(
            listOf("safebooru"), "Safebooru",
            board = SAFEBOORU
        ).register(r)
    }

}

class ImageboardCommand(
    private val commandNames: List<String>,
    private val name: String,
    private val board: ImageBoard<*>,
    private val nsfwOnly: Boolean = false
) : ICommand, ICommand.HelpDialogProvider, ICommand.ExceptionHandler {
    override val category = IMAGEBOARD

    init {
        ImageBoard.throwExceptionOnEOF = false
        boardCmds += commandNames.first()
    }

    override fun toString() = "ImageboardCommand[$name]"

    override fun CommandContext.call() {
        if (nsfwOnly && !requireNSFW()) return

        val args = parseable()

        val (r, p) = args.parseAndCreate<Pair<Rating?, Int>> {
            val rating = option("--rating", "-r") { ratings[takeString()] ?: returnHelp() }
            val page = option("--page", "-p") { tryTakeInt() ?: returnHelp() }

            creator { (rating.resourceOrNull) to (page.resourceOrNull ?: 0) }
        }
        val tags = args.takeAllStrings()

        val fbiDetector = tags.any(fbi::contains)

        val rating = when {
            fbiDetector -> when {
                r == null && !nsfwOnly -> SAFE
                else -> {
                    send(
                        "$WARNING Questionable/Explicit Lolicon/Shotacon images is expressly by Discord.\n" +
                            "\n**Not Allowed Tags**: ${tags.filter(fbi::contains).joinToString("`, `", "`", "`")}" +
                            "\n**Detected Rating**: ${(r ?: EXPLICIT).shortName}"
                    ).queue()
                    return
                }
            }
            else -> if (channel.isNSFW) r else SAFE
        }

        val type = if (tags.isNotEmpty()) SEARCH_RESULT else RANDOM_IMAGE

        val image = when (type) {
            SEARCH_RESULT -> board.search(p, 50, tags.joinToString(" "), rating?.longName)
            RANDOM_IMAGE -> board[p, 50, rating?.longName]
        }.blocking()
            .filter {
                (rating?.equals(it.rating) ?: true)
                    && (it.tags.none(fbi::contains) || it.rating == SAFE)
                    && validExtensions.any { ext -> it.url.endsWith(ext) }
            }
            .shuffled()
            .firstOrNull()

        if (image != null) {
            sendEmbed {
                baseEmbed(event, "$name | ${type.title}", image.url)

                val imgTags = image.tags.sorted().let {
                    if (it.isEmpty()) {
                        "None."
                    } else {
                        val count = AtomicInteger(
                            //initial size to account for the description
                            40 + image.width.toString().length + image.height.toString().length + image.rating.longName.capitalize().length + 2
                        )
                        val tagsTaken = it.takeWhile { tag -> count.addAndGet(tag.length + 3) < 1900 }

                        if (tagsTaken.size != it.size) {
                            tagsTaken.joinToString("`, `", "`", "`, ${it.size - tagsTaken.size} more ...")
                        } else {
                            it.joinToString("`, `", "`", "`")
                        }
                    }
                }

                description(
                    "**${image.width}**x**${image.height}** | **${image.rating.longName.capitalize()}**",
                    "",
                    "**Tags**: $imgTags",
                    "",
                    "**Image**:"
                )
                image(image.url)
            }.queue(null, handleException())
        } else {
            send(
                "$ERROR No images found.\n\n$TALKING Did you use the right tag? Tags are imageboard-dependant and might be different."
            ).queue()
        }
    }

    override fun handle(event: GuildMessageReceivedEvent, t: Throwable) {
        when {
            t is RuntimeException && t.cause is SocketTimeoutException -> {
                // Reported ErrorID: RE#Br4FAL3EAAI
                // Imageboard request times out
                event.channel.sendMessage("$DISAPPOINTED We're having issues with $name. Seems that we can't connect to it. Please, try again later.").queue()
            }
            t is ErrorResponseException -> {
                // Error Message: 485593425605951500
                // Bad image url
                event.channel.sendMessage("$DISAPPOINTED We're having issues with $name. The image that I got was invalid. Please, try again later.").queue()
            }
            t is QueryFailedException -> {
                // Reported ErrorID: QFE#Br4EkZsAAAA
                // Query fails
                event.channel.sendMessage("$DISAPPOINTED We're having issues with $name. Did you put more tags on it than the allowed by the imageboard?").queue()
            }
            else -> throw t
        }
    }

    private val cmd = commandNames.first()

    override val helpHandler by lazy {
        Help(
            CommandDescription(commandNames, "$name Imageboard"),
            Description(
                "Search and get random images from the $name imageboard.",
                if (nsfwOnly) "This command can **only** be used on **NSFW** channels!"
                else "**Questionable/Explicit** images can **only** be viewed on **NSFW** channels!"
            ),
            Usage(
                CommandUsage(cmd, "Gets a random image from $name."),
                CommandUsage("$cmd <tags...>", "Searches $name for a image with the specified tags. The tags **must** be separated by **spaces**.")
            ),
            Note(
                "**Magic Prefixes**:",
                commandUsage("$cmd --page <number> <...>", "Gets a random image from $name on the specified page."),
                commandUsage("$cmd --rating <safe/questionable/explicit> <...>", "Gets a random image from $name on the specified page.")
            ),
            SeeAlso.ofList(boardCmds.filterNot(cmd::equals).sorted())
        )
    }

    fun register(registry: CommandRegistry) {
        registry.register(commandNames, this)
    }

    companion object {
        val boardCmds = ArrayList<String>()
        val validExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")

        val ratings = listOf(
            listOf("safe", "s") to SAFE,
            listOf("questionable", "q") to QUESTIONABLE,
            listOf("explicit", "e") to EXPLICIT
        ).flatMap { (k, v) -> k.map { it to v } }.toMap()

        val fbi = listOf("loli", "shota", "lolicon", "shotacon", "child", "young", "younger", "underage", "under_age")
    }

    enum class Type(val title: String) {
        SEARCH_RESULT("Search Result"),
        RANDOM_IMAGE("Random Image")
    }
}