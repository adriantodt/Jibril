package pw.aru.commands.developer

import ch.qos.logback.core.helpers.ThrowableToStringArray
import mu.KLogging
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.utils.JDALogger
import okhttp3.OkHttpClient
import pw.aru.Aru.sleepQuotes
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.CommandWithArgs
import pw.aru.core.commands.ICommand
import pw.aru.utils.Colors
import pw.aru.utils.J
import pw.aru.utils.api.DiscordBotsPoster
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.LOADING
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import pw.aru.utils.limit
import pw.aru.utils.paste
import java.util.function.Consumer

@Command("dev", "devtools", "hack")
class DevCmd
(
    private val httpClient: OkHttpClient,
    private val shardManager: ShardManager,
    private val statsPoster: DiscordBotsPoster
) : CommandWithArgs<List<String>>(), ICommand.Permission, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override fun args(event: GuildMessageReceivedEvent, args: String) = if (args.isEmpty()) listOf() else args.split(" ", limit = 2)

    override val category = Categories.DEVELOPER
    override val permission = CommandPermission.BOT_DEVELOPER

    override fun call(event: GuildMessageReceivedEvent, args: List<String>) {
        when (args.getOrNull(0)) {
            null, "", "check" -> adminCheck(event)
            "shutdown" -> shutdown(event)
            "eval", "run" -> eval(event, false, args.getOrNull(1) ?: "")
            "peval", "prun" -> eval(event, true, args.getOrNull(1) ?: "")
            "enablecallsite" -> callsite(event, true)
            "disablecallsite" -> callsite(event, false)
            "fixmusic" -> fixmusic(event)
            else -> showHelp()
        }
    }

    private fun fixmusic(event: GuildMessageReceivedEvent) {

    }

    private var disabledCallsiteConsumer = RestAction.DEFAULT_FAILURE
    private val enabledCallsiteConsumer = Consumer<Throwable> {
        JDALogger.getLog(RestAction::class.java).error("RestAction queue returned failure", it)
    }

    private fun callsite(event: GuildMessageReceivedEvent, enable: Boolean) {
        RestAction.setPassContext(enable)

        if (enable) {
            disabledCallsiteConsumer = RestAction.DEFAULT_FAILURE
            RestAction.DEFAULT_FAILURE = enabledCallsiteConsumer
        } else {
            RestAction.DEFAULT_FAILURE = disabledCallsiteConsumer
        }

        event.channel.sendMessage("$SUCCESS Callsite Mode: $enable").queue()
    }

    private val adminJokes = arrayOf(
        "Why didn't you think he wasn't?",
        "They're cute... I mean, yes!",
        "Them? Yeah,"
    )

    private fun adminCheck(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, "Aru Bot | DevTools")
            thumbnail("https://i.imgur.com/gtkaUqF.png") //CHANGE TO ARU_HALO_YES

            description(
                "*${adminJokes.random()}* **${event.member.effectiveName}** is one of my developers${randomOf(".", "!")}"
            )

        }.send(event).queue()
    }

    private fun shutdown(event: GuildMessageReceivedEvent) {
        try {
            statsPoster.postStats()
            event.channel.sendMessage(sleepQuotes.random()).complete()
        } catch (ignored: Exception) {
        }

        return System.exit(0)
    }

    private val sEvals: Map<String, Evaluator> by lazy {
        Evaluators.newStatelessEvaluatorsMap(shardManager)
    }

    private val pEvals: Map<String, PersistentEvaluator> by lazy {
        Evaluators.newPersistentEvaluatorsMap(shardManager)
    }

    private fun listEvals(event: GuildMessageReceivedEvent, persistent: Boolean) {
        embed {
            baseEmbed(event, "DevConsole | Available Evaluators")

            description(
                (if (persistent) pEvals else sEvals)
                    .asSequence()
                    .joinToString("\n\n") { (k, v) -> "``$k`` - ${v.javaClass.simpleName}" }
            )
        }.send(event).queue()
    }

    private fun eval(event: GuildMessageReceivedEvent, persistent: Boolean, args: String) {
        val (eval, code) = with(args.split(" ", limit = 2)) { get(0) to getOrElse(1) { "" } }
        if (eval.isEmpty()) return listEvals(event, persistent)

        val evaluator = (if (persistent) pEvals else sEvals)[eval] ?: return showHelp()

        EmbedFirst(event) {
            baseEmbed("DevConsole | Evaluating...", color = Colors.blurple)
            thumbnail("https://i.imgur.com/Zy2QUI5.png")
            description("$LOADING Evaluating $LOADING")
        } then {
            val (result, e) = try {
                evaluator(event, code) to null
            } catch (e: Exception) {
                null to e
            }

            if (e != null) {
                baseEmbed("DevConsole | Evaluated and errored", color = Colors.discordRed)
                descriptionBuilder.setLength(0)
                field(
                    e.javaClass.name,
                    e.message!!.limit(MessageEmbed.VALUE_MAX_LENGTH)
                )
                field("Full Stacktrace:", paste(httpClient, ThrowableToStringArray.convert(e).joinToString("\n")))
            } else {
                baseEmbed("DevConsole | Evaluated with success", color = Colors.discordGreen)
                description(
                    "Evaluated with success ${if (result == null) "with no objects returned." else "and returned an object."}"
                )
                if (result != null) {
                    val toString = J.toString(result)
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