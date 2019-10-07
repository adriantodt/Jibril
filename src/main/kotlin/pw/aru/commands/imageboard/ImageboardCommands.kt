package pw.aru.commands.imageboard

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.rest.ResponseException
import net.kodehawa.lib.imageboards.DefaultImageBoards.*
import net.kodehawa.lib.imageboards.ImageBoard
import net.kodehawa.lib.imageboards.entities.Rating
import net.kodehawa.lib.imageboards.entities.Rating.*
import net.kodehawa.lib.imageboards.entities.exceptions.QueryFailedException
import pw.aru.bot.CommandRegistry
import pw.aru.bot.categories.Category.IMAGEBOARD
import pw.aru.bot.commands.CommandProvider
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.ICommandProvider
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.parser.parseAndCreate
import pw.aru.bot.parser.tryTakeInt
import pw.aru.commands.imageboard.ImageboardCommand.Type.RANDOM_IMAGE
import pw.aru.commands.imageboard.ImageboardCommand.Type.SEARCH_RESULT
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.styling
import pw.aru.utils.text.DISAPPOINTED
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.TALKING
import pw.aru.utils.text.WARNING
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

@CommandProvider
class ImageboardCommands(private val db: AruDB) : ICommandProvider {

    override fun provide(r: CommandRegistry) {
        ImageboardCommand(
            listOf("rule34", "r34"), "Rule34", db,
            board = RULE34, nsfwOnly = true
        ).register(r)

        // we got banned from e621 because we don't have ratelimit.
        // TODO RATELIMIT IMAGEBOARDS
        //ImageboardCommand(
        //    listOf("e621"), "e621", db,
        //    board = E621, nsfwOnly = true
        //).register(r)

        ImageboardCommand(
            listOf("yandere"), "Yande.re", db,
            board = YANDERE, nsfwOnly = true
        ).register(r)

        ImageboardCommand(
            listOf("konachan"), "Konachan", db,
            board = KONACHAN
        ).register(r)

        ImageboardCommand(
            listOf("danbooru"), "Danbooru", db,
            board = DANBOORU
        ).register(r)

        ImageboardCommand(
            listOf("safebooru"), "Safebooru", db,
            board = SAFEBOORU
        ).register(r)
    }

}

class ImageboardCommand(
    private val commandNames: List<String>,
    private val name: String,
    private val db: AruDB,
    private val board: ImageBoard<*>,
    private val nsfwOnly: Boolean = false
) : ICommand, ICommand.HelpDialogProvider, ICommand.ExceptionHandler {
    override val category = IMAGEBOARD
    override fun nsfw() = nsfwOnly

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

            creator { (rating.orNull) to (page.orNull ?: 0) }
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
                    )
                    return
                }
            }
            else -> if (channel.nsfw()) r else SAFE
        }

        val type = if (tags.isNotEmpty()) SEARCH_RESULT else RANDOM_IMAGE


        val image = runCatching {
            when (type) {
                SEARCH_RESULT -> board.search(p, 50, tags.joinToString(" "), rating)
                RANDOM_IMAGE -> board[p, 50, rating]
            }.blocking()
                .filter {
                    (rating?.equals(it.rating) ?: true)
                            && it.url != null
                            && (it.tags.none(fbi::contains) || it.rating == SAFE)
                            && validExtensions.any { ext -> it.url.endsWith(ext) }
                }
                .shuffled()
                .firstOrNull()
        }.getOrNull()

        if (image != null) {
            withMDC("image" to image.toString()) {
                sendEmbed {
                    styling(message)
                        .author("$name | ${type.title}", image.url)
                        .applyAll()

                    val showInfo = UserSettings(db, author.idAsLong()).showImageboardInfo
                        ?: GuildSettings(db, guild.idAsLong()).showImageboardInfo

                    if (showInfo) {
                        val imgTags = image.tags.sorted().let {
                            if (it.isEmpty()) {
                                "None."
                            } else {
                                val count = AtomicInteger(
                                    //initial size to account for the description
                                    40 + image.width.toString().length + image.height.toString().length + image.rating.longName.capitalize().length + 2
                                )
                                val tagsTaken = it.takeWhile { tag -> count.addAndGet(tag.length + 3) < 1800 }

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
                    }

                    image(image.url)
                }.whenComplete(handlingExceptions())
            }
        } else {
            send(
                "$ERROR No images found.\n\n$TALKING Did you use the right tag? Tags are imageboard-dependant and might be different."
            )
        }
    }

    override fun handle(message: Message, t: Throwable) {
        when {
            t is RuntimeException && t.cause is SocketTimeoutException -> {
                // Reported ErrorID: RE#Br4FAL3EAAI
                // Imageboard request times out
                message.channel()
                    .sendMessage("$DISAPPOINTED We're having issues with $name. Seems that we can't connect to it. Please, try again later.")
            }
            t is ResponseException -> {
                // Error Message: 485593425605951500
                // Bad image url
                message.channel()
                    .sendMessage("$DISAPPOINTED We're having issues with $name. The image that I got was invalid. Please, try again later.")
            }
            t is QueryFailedException -> {
                // Reported ErrorID: QFE#Br4EkZsAAAA
                // Query fails
                message.channel()
                    .sendMessage("$DISAPPOINTED We're having issues with $name. Did you put more tags on it than the allowed by the imageboard?")
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
                CommandUsage(
                    "$cmd <tags...>",
                    "Searches $name for a image with the specified tags. The tags **must** be separated by **spaces**."
                )
            ),
            Note(
                "**Magic Prefixes**:",
                commandUsage("$cmd --page <number> <...>", "Gets a random image from $name on the specified page."),
                commandUsage(
                    "$cmd --rating <safe/questionable/explicit> <...>",
                    "Gets a random image from $name on the specified page."
                )
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