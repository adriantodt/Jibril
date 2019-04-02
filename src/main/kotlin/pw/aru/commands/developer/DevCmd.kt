package pw.aru.commands.developer

import com.github.natanbc.weeb4j.Weeb4J
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.user.User
import com.mewna.catnip.rest.invite.InviteCreateOptions
import mu.KLogging
import pw.aru.AruBot.sleepQuotes
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.hypervisor.AruHypervisor
import pw.aru.core.parser.*
import pw.aru.core.permissions.Permissions
import pw.aru.core.permissions.UserPermissions.BOT_DEVELOPER
import pw.aru.core.reporting.LocalPastes.paste
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.extensions.lang.SplitPolicy
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.extensions.lang.smartSplit
import pw.aru.utils.text.SHRUG
import pw.aru.utils.text.SUCCESS
import java.lang.System.currentTimeMillis
import java.lang.reflect.Modifier

@Command("dev")
class DevCmd
    (
    private val db: AruDB,
    //private val musicManager: MusicManager,
    registry: CommandRegistry,
    weebSh: Weeb4J,
    private val hypervisor: AruHypervisor,
    private val assetProvider: ReloadableListProvider
) : ICommand, ICommand.Permission, ICommand.HelpDialogProvider {
    private val contentGenerator = ContentGenerator(registry)
    private val devWeebSh = DevWeebSh(weebSh)
    private val devEval = DevEval(db, registry)

    companion object : KLogging()

    override val category = Category.DEVELOPER
    override val permissions = Permissions.Just(BOT_DEVELOPER)

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "shutdown" -> shutdown()

            "legacypremium", "premium" -> when (args.takeString()) {
                "user", "u" -> userLegacyPremium(args)
                "guild", "g" -> guildLegacyPremium(args)
                else -> showHelp()
            }

            "mkinvite" -> makeInvite(args)

            "eval", "run" -> devEval(this, args)

            "weebsh" -> devWeebSh(this, args)

            "reloadassets" -> reloadassets()

            "gen" -> when (args.takeString()) {
                "cmd.yaml", "cmd.yml" -> {
                    send(
                        "**Commands.yml generated**: ${paste(
                            "Commands.yml generated:",
                            contentGenerator.generateCmdYaml()
                        )}"
                    )
                }
                "cmd.zip" -> {
                    sendMessage {
                        content("**Commands.zip generated:**")
                        addFile("commands.zip", contentGenerator.generateCmdZip())
                    }
                }
                "cmd.html" -> {
                    send(
                        "**Commands.html snippet generated**: ${paste(
                            "Commands.html generated:",
                            contentGenerator.generateCmdHtml()
                        )}"
                    )
                }
                "cmd.md" -> {
                    send(
                        "**Commands.md snippet generated**: ${paste(
                            "Commands.md generated:",
                            contentGenerator.generateCmdMd()
                        )}"
                    )
                }
                else -> showHelp()
            }

            "genemotelist" -> generateEmoteList()

            else -> showHelp()
        }
    }

    private fun CommandContext.makeInvite(args: Args) {
        (args.tryTakeTextChannel(guild) ?: channel)
            .createInvite(InviteCreateOptions())
            .thenCombine(author.createDM()) { invite, channel ->
                channel.sendMessage("https://discord.gg/${invite.code()}")
            }
    }

    private fun CommandContext.userLegacyPremium(args: Args) {
        val userId = args.tryTakeMember(guild)?.idAsLong() ?: args.tryTakeLong() ?: return showHelp()

        // existencial crysis
        val user: User? = catnip.cache().user(userId)

        if (user == null) {
            send("$SHRUG User not found!")
            return
        }

        val userSettings = UserSettings(db, userId)

        if (args.matchNextString("remove")) {
            userSettings.legacyPremium = false
            send("$SUCCESS **LegacyPremium**: User ``${user.discordTag()} (${user.id()})`` is no longer Premium.")
        } else {
            userSettings.legacyPremium = true
            userSettings.premiumSince = currentTimeMillis()
            args.tryTakeInt().let { if (it != null) userSettings.premiumAmount = it }

            send("$SUCCESS **LegacyPremium**: User ``${user.discordTag()} (${user.id()})`` is now Premium!")
        }
    }

    private fun CommandContext.guildLegacyPremium(args: Args) {
        val guildId = args.tryTakeLong() ?: return showHelp()

        // existencial crysis
        val guild: Guild? = catnip.cache().guild(guildId)

        if (guild == null) {
            send("$SHRUG Guild not found!")
            return
        }

        val guildSettings = GuildSettings(db, guildId)

        if (args.matchNextString("remove")) {
            guildSettings.legacyPremium = false
            send("$SUCCESS **LegacyPremium**: Guild ``${guild.name()} (${guild.id()})`` is no longer Premium.")
        } else {
            guildSettings.legacyPremium = true
            guildSettings.premiumSince = currentTimeMillis()

            send("$SUCCESS **LegacyPremium**: Guild ``${guild.name()} (${guild.id()})`` is now Premium!")
        }
    }

    private fun CommandContext.generateEmoteList() {
        multiline(
            "**EMOTES**",
            Class.forName("pw.aru.utils.emotes.Emotes").declaredFields.asSequence()
                .filter { Modifier.isPublic(it.modifiers) }
                .map { it.name to it[null].toString() }
                .sortedBy(Pair<String, String>::first)
                .joinToString("\n") { "${it.second} | ${it.first}" }
        ).smartSplit(2000, SplitPolicy.NEWLINE).map(::send)
    }

    private fun CommandContext.shutdown() {
        try {
            hypervisor.onBotShutdown(catnip)
            send(sleepQuotes.random()).toCompletableFuture().join()
        } catch (ignored: Exception) {
        }

        return System.exit(0)
    }

    private fun CommandContext.reloadassets() {
        assetProvider.reload()
        message.react(SUCCESS)
    }

    override val helpHandler = Help(
        CommandDescription(listOf("dev"), "Developer Command", permissions),
        Usage(
            CommandUsage("dev shutdown", "Shutdown."),
            UsageSeparator,
            CommandUsage("dev premium user <@mention/id> [remove]", "Set user as Premium."),
            CommandUsage("dev premium guild <@mention/id> [remove]", "Set guild as Premium."),
            UsageSeparator,
            CommandUsage("dev <eval/run> <engine> <code>", "Evals a piece of code."),
            UsageSeparator,
            CommandUsage("dev <enablecallsite/disablecallsite>", "Callsite tracing."),
            UsageSeparator,
            CommandUsage("dev weebsh", "Weeb.sh Dump."),
            CommandUsage("dev weebsh -account", "Weeb.sh Account Dump."),
            CommandUsage(
                "dev weebsh <[-type <...>] [-tags <...>] [-ext <...>] [-hidden <...>] [-nsfw <...>]>",
                "Gets a random Weeb.sh image."
            ),
            UsageSeparator,
            CommandUsage("dev peek nowplaying", "Peek musics playing."),
            UsageSeparator,
            CommandUsage("dev reloadassets", "Reload assets."),
            CommandUsage("dev genemotelist", "EmotesKt dump."),
            CommandUsage("dev gencmdyml", "Generate commands.yml file."),
            CommandUsage("dev <gencmdhtml/gencmdmd>", "Generate snipppet. (botlists)")
        )
    )
}
