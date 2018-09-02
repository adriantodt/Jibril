package pw.aru.commands.developer

import com.github.natanbc.weeb4j.Account
import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.NsfwFilter
import gnu.trove.TDecorators.wrap
import mu.KLogging
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy.NEWLINE
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.requests.restaction.MessageAction
import net.dv8tion.jda.core.utils.JDALogger
import okhttp3.OkHttpClient
import pw.aru.Aru.sleepQuotes
import pw.aru.commands.actions.base.GetImage
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicManager
import pw.aru.core.parser.Args
import pw.aru.core.parser.parseAndCreate
import pw.aru.core.parser.tryTakeInt
import pw.aru.db.AruDB
import pw.aru.exported.aru_version
import pw.aru.utils.Colors
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.api.DBLPoster
import pw.aru.utils.api.DBotsPoster
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import pw.aru.utils.limit
import pw.aru.utils.paste
import java.lang.reflect.Modifier
import java.util.function.Consumer

@Command("dev", "devtools", "hack")
@UseFullInjector
class DevCmd
(
    private val httpClient: OkHttpClient,
    private val shardManager: ShardManager,
    db: AruDB,
    private val musicManager: MusicManager,
    private val registry: CommandRegistry,
    private val weebSh: Weeb4J,
    private val dblPoster: DBLPoster,
    private val dpwPoster: DBotsPoster,
    private val assetProvider: ReloadableListProvider
) : ICommand, ICommand.Permission, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Category.DEVELOPER
    override val permission = CommandPermission.BOT_DEVELOPER

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "crash" -> throw RuntimeException("I'm not feeling good, Todt~")
            "shitlog" -> {
                logger.info("Here's a shitton of numbers: ${(0..10000).joinToString()}")
            }

            "shutdown" -> shutdown()

            "eval", "run" -> eval(false, args.takeRemaining())
            "peval", "prun" -> eval(true, args.takeRemaining())

            "enablecallsite" -> callsite(true)
            "disablecallsite" -> callsite(false)

            "weebsh" -> weebsh(args)

            "peek" -> when (args.takeString()) {
                "nowplaying" -> peekNowPlaying(args)
                else -> showHelp()
            }

            "reloadassets" -> reloadassets()
            "genwebyml" -> generateWebYaml()
            "gencmdhtml" -> generateCmdHtml()
            "gencmdmd" -> generateCmdMd()
            "genemotelist" -> generateEmoteList()
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

    private fun CommandContext.peekNowPlaying(args: Args) {
        sendEmbed {
            baseEmbed(event, "Aru! | Peek: NowPlaying")
            wrap(musicManager.musicPlayers)
                .entries
                .asSequence()
                .filter { (_, p) -> p.currentChannel != null && p.currentTrack != null }
                .sortedByDescending { it.value.queue.size }
                .drop(args.tryTakeInt()?.minus(1)?.times(10) ?: 0)
                .take(10)
                .forEach { (guildId, player) ->
                    val guild = shardManager.getGuildById(guildId)
                    val nowPlaying = player.currentTrack!!.info
                    val voiceChannel = guild.selfMember.voiceState.channel
                    field(
                        "Guild: ${guild.name} (${guild.id})",
                        "**Voice Channel**: ${voiceChannel.name} (${voiceChannel.humanUsers} listening)",
                        "",
                        "**Now Playing**: ",
                        "**[${nowPlaying.title.limit(40)}](${nowPlaying.uri})** by **${nowPlaying.author}**",
                        if (player.queue.isEmpty()) "Empty queue."
                        else "**Queued**:\n" + player.queue.take(3).joinToString("\n") { "**[${it.info.title.limit(40)}](${it.info.uri})** by **${it.info.author}**" }
                    )
                }
        }.queue()
    }

    private fun CommandContext.generateEmoteList() {
        MessageBuilder()
            .append("**EMOTES**\n")
            .append(
                Class.forName("pw.aru.utils.emotes.Emotes")
                    .declaredFields
                    .filter { Modifier.isPublic(it.modifiers) }
                    .map { it.name to it[null].toString() }
                    .sortedBy(Pair<String, String>::first)
                    .joinToString("\n") { "${it.second} | ${it.first}" }
            )
            .buildAll(NEWLINE)
            .asSequence()
            .map(::send)
            .forEach(MessageAction::queue)
    }

    private fun CommandContext.weebsh(args: Args) {
        if (args.matchNextString("-account")) {
            return weebshAccount()
        }

        val (image, nsfw) = args.parseAndCreate<Pair<GetImage, NsfwFilter?>> {
            val type = option("-type") { takeString() }
            val tags = option("-tags") { takeString().split(',') }
            val ext = option("-ext") { FileType.valueOf(takeString().toUpperCase()) }
            val nsfw = option("-nsfw") { NsfwFilter.valueOf(("${takeString()}_NSFW").toUpperCase()) }

            creator { GetImage(type.resourceOrNull, tags.resourceOrNull, ext.resourceOrNull) to nsfw.resourceOrNull }
        }

        if (image.isNotEmpty()) {
            return weebshGet(image, nsfw)
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
        weebSh.imageProvider.getRandomImage(img.type, img.tags, null, nsfw, img.fileType).async { image ->
            if (image == null) {
                send("$CONFUSED No images found... ").queue()
            } else {
                sendEmbed {
                    baseEmbed(event, "Aru! | Weeb.sh Debug")
                    image(image.url)
                    description(
                        "Type: ${image.type}",
                        "Tags: ${image.tags.joinToString(", ", "[", "]") { "Tag[name=${it.name}, user=${it.user}]" }}",
                        "Account: ${image.account}"
                    )
                }.queue()
            }
        }
    }

    private fun CommandContext.weebshAccount() {
        sendEmbed {
            baseEmbed(event, "Aru! | Weeb.sh Debug")
            thumbnail("https://assets.aru.pw/img/yes.png")

            val (id, name, discordId, active, scopes) = weebSh.tokenInfo.execute().account

            description(
                "ID: ``$id``",
                "Name: ``$name``",
                "DiscordID: ``$discordId``",
                "Active: ${active.toString().toLowerCase().capitalize()}",
                "",
                "Scopes:",
                "```",
                scopes.sorted().joinToString(" "),
                "```"
            )
        }.queue()
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
                field("Full Stacktrace:", paste("Full Stacktrace:", e.stackTraceToString()), "java")
            } else {
                baseEmbed("DevConsole | Evaluated with success", color = Colors.discordGreen)
                thumbnail("https://assets.aru.pw/img/yes.png")
                description(
                    "Evaluated with success ${if (result == null) "with no objects returned." else "and returned an object."}"
                )
                if (result != null) {
                    val toString = result.toPrettyString()
                    field(
                        result.javaClass.simpleName,
                        toString.limit(MessageEmbed.VALUE_MAX_LENGTH)
                    )

                    if (toString.length > MessageEmbed.VALUE_MAX_LENGTH) {
                        field("Full ToString:", paste("Full toString():", toString))
                    }
                }
            }

            footer("Evaluated by: ${event.author.name}", event.author.effectiveAvatarUrl)
        }
    }

    private fun CommandContext.generateWebYaml() {
        val cmdsGroup = registry.lookup.entries.groupBy { it.key.category }
        val builder = StringBuilder()
        for (category in Category.LIST) {
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "- name: ${category.categoryName}\n  list:\n"
            for ((cmd, cmdNames) in cmds) {
                builder += "    - cmd: ${cmdNames[0]}\n"
                if (cmdNames.size > 1) {
                    builder += "      alias: ${cmdNames.drop(1).joinToString(" ")}\n"
                }
                builder += "      desc: TODO\n"
                builder += "\n"
            }
            builder += "\n"
        }

        send("**Commands.yml generated**: ${paste("Commands.yml generated:", builder.toString())}").queue()
    }

    private fun CommandContext.generateCmdHtml() {
        val cmdsGroup = registry.lookup.entries.groupBy({ it.key.category }, { it.value[0] })
        val builder = StringBuilder()
        builder += "<p class=\"fmt-h4\">My Commands: (v$aru_version)</p>\n<ul class=\"bot-list\">\n"

        for (category in Category.LIST) {
            if (category.nsfw || category.permission == CommandPermission.BOT_DEVELOPER) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "<li><b class=\"fmt-b\">${category.categoryName}</b>: ${cmds.sorted().joinToString("</code> <code>", "<code>", "</code>")}</li>\n"
        }

        builder += "</ul>\n"
        send("**Commands.html snippet generated**: ${paste("Commands.html generated:", builder.toString())}").queue()
    }

    private fun CommandContext.generateCmdMd() {
        val cmdsGroup = registry.lookup.entries.groupBy({ it.key.category }, { it.value[0] })
        val builder = StringBuilder()
        builder += "### My Commands: (v$aru_version)\n\n"

        for (category in Category.LIST) {
            if (category.nsfw || category.permission == CommandPermission.BOT_DEVELOPER) continue
            val cmds = cmdsGroup[category] ?: continue
            if (cmds.isEmpty()) continue

            builder += "- **${category.categoryName}**: ${cmds.sorted().joinToString("` `", "`", "`")}\n"
        }

        builder += "\n"
        send("**Commands.md snippet generated**: ${paste("Commands.md generated:", builder.toString())}").queue()
    }

    private fun CommandContext.reloadassets() {
        assetProvider.reload()
        message.addReaction(SUCCESS).queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("dev", "devtools", "hack"), "Developer Command", permission),
        Usage(
            CommandUsage("dev [check]", "Checks if the user running this command is one of my developers."),
            CommandUsage("dev shutdown", "Shutdowns the bot."),
            UsageSeparator,
            CommandUsage("dev <eval/run> <engine> <code>", "Evals a piece of code."),
            CommandUsage("dev <peval/prun> <engine> <code>", "Evals a piece of code in a persistent environiment."),
            UsageSeparator,
            CommandUsage("dev enablecallsite", "Evals a piece of code in a persistent environiment."),
            CommandUsage("dev disablecallsite", "Evals a piece of code in a persistent environiment."),
            UsageSeparator,
            CommandUsage("dev weebsh", "Dumps Weeb.sh types and tags."),
            CommandUsage("dev weebsh <[-type <value>] [-tags <values,...>] [-nsfw <value>] [-ext <value>]>", "Gets a random Weeb.sh image."),
            UsageSeparator,
            CommandUsage("dev peek nowplaying", "Peek musics being played."),
            UsageSeparator,
            CommandUsage("dev reloadassets", "Reloads assets."),
            CommandUsage("dev genwebyml", "Generates the base commands.yml file."),
            CommandUsage("dev gendcmdhtml", "Generates the html snipppet for the botlists."),
            CommandUsage("dev gendcmdmd", "Generates the markdown snipppet for the botlists.")
        )
    )
}

private operator fun Account.component1() = id
private operator fun Account.component2() = name
private operator fun Account.component3() = discordId
private operator fun Account.component4() = isActive
private operator fun Account.component5() = scopes
