package pw.aru.commands.developer

import com.github.natanbc.weeb4j.Account
import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.HiddenMode.DEFAULT
import com.github.natanbc.weeb4j.image.NsfwFilter
import com.github.natanbc.weeb4j.image.NsfwFilter.NO_NSFW
import gnu.trove.TDecorators.wrap
import mu.KLogging
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.MessageBuilder.SplitPolicy.NEWLINE
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.requests.restaction.MessageAction
import net.dv8tion.jda.core.utils.JDALogger
import okhttp3.OkHttpClient
import pw.aru.Aru.Companion.sleepQuotes
import pw.aru.commands.actions.impl.ImageBasedCommandImpl
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.hypervisor.AruHypervisor
import pw.aru.core.music.MusicManager
import pw.aru.core.parser.*
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings
import pw.aru.exported.aru_version
import pw.aru.utils.Colors
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.SHRUG
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
    private val db: AruDB,
    private val musicManager: MusicManager,
    private val registry: CommandRegistry,
    private val weebSh: Weeb4J,
    private val hypervisor: AruHypervisor,
    private val assetProvider: ReloadableListProvider
) : ICommand, ICommand.Permission, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Category.DEVELOPER
    override val permission = CommandPermission.BOT_DEVELOPER

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "shutdown" -> shutdown()

            "legacypremium", "premium" -> when (args.takeString()) {
                "user", "u" -> userLegacyPremium(args)
                "guild", "g" -> guildLegacyPremium(args)
                else -> showHelp()
            }

            "eval", "run" -> eval(args.takeRemaining())

            "enablecallsite" -> callsite(true)
            "disablecallsite" -> callsite(false)

            "weebsh" -> weebsh(args)

            "peek" -> when (args.takeString()) {
                "nowplaying", "np" -> peekNowPlaying(args)
                else -> showHelp()
            }

            "reloadassets" -> reloadassets()
            "genemotelist" -> generateEmoteList()
            "gencmdyml" -> generateCmdYaml()
            "gencmdhtml" -> generateCmdHtml()
            "gencmdmd" -> generateCmdMd()

            "", "check" -> adminCheck()
            else -> showHelp()
        }
    }

    private fun CommandContext.userLegacyPremium(args: Args) {
        val userId = args.tryTakeMember(guild)?.user?.idLong ?: args.tryTakeLong() ?: return showHelp()

        // existencial crysis
        val user: User? = shardManager.getUserById(userId)

        if (user == null) {
            send("$SHRUG User not found!").queue()
            return
        }

        val userSettings = UserSettings(db, userId)

        if (args.matchNextString("remove")) {
            userSettings.legacyPremium = false
            send("$SUCCESS **LegacyPremium**: User ``${user.discordTag} (${user.id})`` is no longer Premium.").queue()
        } else {
            userSettings.legacyPremium = true
            send("$SUCCESS **LegacyPremium**: User ``${user.discordTag} (${user.id})`` is now Premium!").queue()
        }
    }

    private fun CommandContext.guildLegacyPremium(args: Args) {
        val guildId = args.tryTakeLong() ?: return showHelp()

        // existencial crysis
        val guild: Guild? = shardManager.getGuildById(guildId)

        if (guild == null) {
            send("$SHRUG Guild not found!").queue()
            return
        }

        val guildSettings = GuildSettings(db, guildId)

        if (args.matchNextString("remove")) {
            guildSettings.legacyPremium = false
            send("$SUCCESS **LegacyPremium**: Guild ``${guild.name} (${guild.id})`` is no longer Premium.").queue()
        } else {
            guildSettings.legacyPremium = true
            send("$SUCCESS **LegacyPremium**: Guild ``${guild.name} (${guild.id})`` is now Premium!").queue()
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
            wrap(musicManager.musicPlayers).entries.asSequence()
                .filter { (_, p) -> p.currentChannel != null && p.currentTrack != null }
                .sortedByDescending { it.value.currentChannel!!.humanUsers }
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
                Class.forName("pw.aru.utils.emotes.Emotes").declaredFields.asSequence()
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

        val image = args.parseAndCreate<GetImage> {
            val type = option("-type") { takeString() }
            val tags = option("-tags") { takeString().split(',') }
            val ext = option("-ext") { FileType.valueOf(takeString().toUpperCase()) }
            val hidden = option("-hidden") { HiddenMode.valueOf(takeString().toUpperCase()) }
            val nsfw = option("-nsfw") { NsfwFilter.valueOf(("${takeString()}_NSFW").toUpperCase()) }

            creator {
                GetImage(
                    type.resourceOrNull,
                    tags.resourceOrNull,
                    ext.resourceOrNull,
                    hidden.resourceOrNull ?: DEFAULT,
                    nsfw.resourceOrNull ?: NO_NSFW
                )
            }
        }

        if (image.isNotEmpty()) {
            return weebshGet(image)
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

    private fun CommandContext.weebshGet(img: GetImage) {
        weebSh.imageProvider.getRandomImage(img.type, img.tags, img.hidden, img.nsfw, img.ext).async { image ->
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
            hypervisor.onBotShutdown(shardManager)
            send(sleepQuotes.random()).complete()
        } catch (ignored: Exception) {
        }

        return System.exit(0)
    }

    private val evals: Map<String, Evaluator> = mapOf(
        "js" to JsEvaluator(shardManager, db, registry),
        "bsh" to BshEvaluator(shardManager, db, registry)
    )

    private fun CommandContext.listEvals() {
        sendEmbed {
            baseEmbed(event, "DevConsole | Available Evaluators")
            description(evals.entries.joinToString("\n\n") { (k, v) -> "``$k`` - ${v.javaClass.simpleName}" })
        }.queue()
    }

    private val evaluatingQuotes = arrayOf(
        "Creating Pylons of Java...",
        "Warming up compilers...",
        "Starting up Reflections...",
        "Building Abstract Syntax Trees...",
        "Recursively interpreting code..."
    )

    private fun CommandContext.eval(args: String) {
        val (eval, code) = args.split(" ", limit = 2).run { get(0) to getOrElse(1) { "" } }
        if (eval.isEmpty()) return listEvals()

        val evaluator = evals[eval] ?: return showHelp()

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

    private fun CommandContext.generateCmdYaml() {
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

                if (cmd is ImageBasedCommandImpl) {
                    builder += "      desc: \"${cmd.description.replaceEach("\n" to "\\\n", "\"" to "\\\"")}\"\n"
                } else if (cmd is ICommand.HelpDialogProvider) {
                    val handler = cmd.helpHandler
                    builder += "      desc: \"${
                    if (handler is Help) (handler.nodes.firstOrNull { it is Description } as? Description)?.value?.replaceEach("\n" to "\\\n", "\"" to "\\\"")
                        ?: "TODO" else "TODO"
                    }\"\n"
                } else {
                    builder += "      desc: TODO\n"
                }

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
            CommandUsage("dev premium user <@mention/id>", "Sets an user as Premium."),
            CommandUsage("dev premium user <@mention/id> remove", "Removes the Premium status from an user."),
            CommandUsage("dev premium guild <@mention/id>", "Sets a guilds as Premium."),
            CommandUsage("dev premium guild <@mention/id> remove", "Removes the Premium status from a guild."),
            UsageSeparator,
            CommandUsage("dev <eval/run> <engine> <code>", "Evals a piece of code."),
            UsageSeparator,
            CommandUsage("dev enablecallsite", "Enables Callsite tracing."),
            CommandUsage("dev disablecallsite", "Disables Callsite tracing."),
            UsageSeparator,
            CommandUsage("dev weebsh", "Dumps Weeb.sh types and tags."),
            CommandUsage("dev weebsh -account", "Prints Weeb.sh account information."),
            CommandUsage("dev weebsh <[-type <value>] [-tags <values,...>] [-ext <value>] [-hidden <value>] [-nsfw <value>]>", "Gets a random Weeb.sh image."),
            UsageSeparator,
            CommandUsage("dev peek nowplaying", "Peek musics being played."),
            UsageSeparator,
            CommandUsage("dev reloadassets", "Reloads assets."),
            CommandUsage("dev genemotelist", "Dumps Aru's 'emotes.kt' file on the text channel."),
            CommandUsage("dev gencmdyml", "Generates the base commands.yml file."),
            CommandUsage("dev gencmdhtml", "Generates the html snipppet for the botlists."),
            CommandUsage("dev gencmdmd", "Generates the markdown snipppet for the botlists.")
        )
    )
}

private operator fun Account.component1() = id
private operator fun Account.component2() = name
private operator fun Account.component3() = discordId
private operator fun Account.component4() = isActive
private operator fun Account.component5() = scopes

data class GetImage(val type: String?, val tags: List<String>?, val ext: FileType?, val hidden: HiddenMode = DEFAULT, val nsfw: NsfwFilter = NO_NSFW) {
    fun isNotEmpty() = (type != null && type.isNotEmpty()) || (tags != null && tags.isNotEmpty())
}