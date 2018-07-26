package pw.aru.commands.developer

import ch.qos.logback.core.helpers.ThrowableToStringArray
import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.NsfwFilter
import mu.KLogging
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.utils.JDALogger
import okhttp3.OkHttpClient
import pw.aru.Aru.sleepQuotes
import pw.aru.commands.actions.base.GetImage
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.parser.Args
import pw.aru.db.AruDB
import pw.aru.utils.Colors
import pw.aru.utils.api.DBLPoster
import pw.aru.utils.api.DBotsPoster
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import pw.aru.utils.limit
import pw.aru.utils.paste
import java.util.function.Consumer

@Command("dev", "devtools", "hack")
@UseFullInjector
class DevCmd
(
    private val httpClient: OkHttpClient,
    shardManager: ShardManager,
    db: AruDB,
    private val weebSh: Weeb4J,
    private val dblPoster: DBLPoster,
    private val dpwPoster: DBotsPoster
) : ICommand, ICommand.Permission, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Categories.DEVELOPER
    override val permission = CommandPermission.BOT_DEVELOPER

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "shutdown" -> shutdown()
            "eval", "run" -> eval(false, args.takeRemaining())
            "peval", "prun" -> eval(true, args.takeRemaining())
            "enablecallsite" -> callsite(true)
            "disablecallsite" -> callsite(false)
            "weebsh" -> weebsh(args)
            "", "check" -> adminCheck()
            else -> showHelp()
        }
    }

    private var disabledCallsiteConsumer = RestAction.DEFAULT_FAILURE
    private val enabledCallsiteConsumer = Consumer<Throwable> {
        JDALogger.getLog(RestAction::class.java).error("RestAction queue returned failure", it)
    }

    private fun CommandContext.callsite(enable: Boolean) {
        RestAction.setPassContext(enable)

        if (enable) {
            disabledCallsiteConsumer = RestAction.DEFAULT_FAILURE
            RestAction.DEFAULT_FAILURE = enabledCallsiteConsumer
        } else {
            RestAction.DEFAULT_FAILURE = disabledCallsiteConsumer
        }

        send("$SUCCESS Callsite Mode: $enable").queue()
    }

    private val adminJokes = arrayOf(
        "Why didn't you think he wasn't?",
        "They're cute... I mean, yes!",
        "Them? Yeah,"
    )

    private fun CommandContext.adminCheck() {
        sendEmbed {
            baseEmbed(event, "Aru! | DevTools")
            thumbnail("https://assets.aru.pw/img/yes.png")

            description(
                "*${adminJokes.random()}* **${event.member.effectiveName}** is one of my developers${randomOf(".", "!")}"
            )

        }.queue()
    }

    private fun CommandContext.weebsh(args: Args) {
        val type = if (args.matchNextString("-type"::equals)) args.takeString() else null
        val tags = if (args.matchNextString("-tags"::equals)) args.takeString().split(',') else null
        val ext = if (args.matchNextString("-ext"::equals)) FileType.valueOf(args.takeString().toUpperCase()) else null
        val nsfw = if (args.matchNextString("-nsfw"::equals)) NsfwFilter.valueOf(("${args.takeString()}_NSFW").toUpperCase()) else null

        if (type != null || tags != null) {
            return weebshGet(GetImage(type, tags, ext), nsfw)
        }

        val imageTypes = weebSh.imageProvider.imageTypes.submit()
        val imageTags = weebSh.imageProvider.imageTags.submit()
        sendEmbed {
            baseEmbed(event, "Aru! | Weeb.sh Debug")
            thumbnail("https://assets.aru.pw/img/yes.png")

            description(
                "Types:",
                "```",
                imageTypes().types.sorted().joinToString(" "),
                "```",
                "",
                "Tags:",
                "```",
                imageTags().sorted().joinToString(" "),
                "```"
            )
        }.queue()
    }

    private fun CommandContext.weebshGet(img: GetImage, nsfw: NsfwFilter?) {
        weebSh.imageProvider.getRandomImage(img.type, img.tags, null, nsfw, img.fileType).async {
            if (it == null) {
                send("$CONFUSED No images found... ").queue()
            } else {
                sendEmbed {
                    baseEmbed(event, "Aru! | Weeb.sh Debug")
                    image(it.url)
                    description(
                        "Type: ${it.type}",
                        "Tags: ${it.tags.joinToString(", ", "[", "]") { "Tag[name=${it.name}, user=${it.user}]" }}",
                        "Account: ${it.account}"
                    )
                }.queue()
            }
        }
    }

    private fun CommandContext.shutdown() {
        try {
            dblPoster.postStats()
            dpwPoster.postStats()
            send(sleepQuotes.random()).complete()
        } catch (ignored: Exception) {
        }

        return System.exit(0)
    }

    private val sEvals: Map<String, Evaluator> by lazy {
        Evaluators.newStatelessEvaluatorsMap(shardManager, db)
    }

    private val pEvals: Map<String, PersistentEvaluator> by lazy {
        Evaluators.newPersistentEvaluatorsMap(shardManager, db)
    }

    private fun CommandContext.listEvals(persistent: Boolean) {
        sendEmbed {
            baseEmbed(event, "DevConsole | Available Evaluators")

            description(
                (if (persistent) pEvals else sEvals)
                    .asSequence()
                    .joinToString("\n\n") { (k, v) -> "``$k`` - ${v.javaClass.simpleName}" }
            )
        }.queue()
    }

    private val evaluatingQuotes = arrayOf(
        "Creating Pylons of Java...",
        "Warming up compilers...",
        "Starting up Reflections...",
        "Building Abstract Syntax Trees...",
        "Recursively interpreting code..."
    )

    private fun CommandContext.eval(persistent: Boolean, args: String) {
        val (eval, code) = with(args.split(" ", limit = 2)) { get(0) to getOrElse(1) { "" } }
        if (eval.isEmpty()) return listEvals(persistent)

        val evaluator = (if (persistent) pEvals else sEvals)[eval] ?: return showHelp()

        EmbedFirst(event) {
            baseEmbed("DevConsole | Evaluating...", color = Colors.blurple)
            thumbnail("https://assets.aru.pw/img/loading.gif")
            description("*${evaluatingQuotes.random()}*")
        } then {
            val (result, e) = try {
                evaluator(event, code) to null
            } catch (e: Exception) {
                null to e
            }

            if (e != null) {
                baseEmbed("DevConsole | Evaluated and errored", color = Colors.discordRed)
                thumbnail("https://assets.aru.pw/img/no.png")
                descriptionBuilder.setLength(0)
                field(
                    e.javaClass.name,
                    e.message!!.limit(MessageEmbed.VALUE_MAX_LENGTH)
                )
                field("Full Stacktrace:", paste(httpClient, ThrowableToStringArray.convert(e).joinToString("\n")))
            } else {
                baseEmbed("DevConsole | Evaluated with success", color = Colors.discordGreen)
                thumbnail("https://assets.aru.pw/img/yes.png")
                description(
                    "Evaluated with success ${if (result == null) "with no objects returned." else "and returned an object."}"
                )
                if (result != null) {
                    val toString = result.advancedToString()
                    field(
                        result.javaClass.simpleName,
                        toString.limit(MessageEmbed.VALUE_MAX_LENGTH)
                    )

                    if (toString.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        field("Full ToString:", paste(httpClient, toString))
                    }
                }
            }

            footer("Evaluated by: ${event.author.name}", event.author.effectiveAvatarUrl)
        }
    }

    override val helpHandler = HelpFactory("Developer Command", permission) {
        aliases("devtools", "hack")
        usage("dev [check]", "Checks if the user running this command is one of my developers.")
        usage("dev shutdown", "Shutdowns the bot.")
        usage("dev <eval/run> <engine> <code>", "Evals a piece of code.")
        usage("dev <peval/prun> <engine> <code>", "Evals a piece of code in a persistent environiment.")
    }
}