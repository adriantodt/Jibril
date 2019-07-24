package pw.aru.bot

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission.*
import mu.KLogging
import pw.aru.Aru
import pw.aru.Aru.*
import pw.aru.Aru.Bot.prefixes
import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.entities.guild.GuildSettings
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.ICommand.ExceptionHandler
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.context.CommandContext.ShowHelp
import pw.aru.bot.parser.Args.Companion.SPLIT_CHARS
import pw.aru.bot.permissions.PermissionResolver
import pw.aru.bot.reporting.ErrorReporter
import pw.aru.core.permissions.Permission
import pw.aru.core.permissions.UserPermissions.USE_BOT
import pw.aru.utils.CommandStatsManager
import pw.aru.utils.extensions.aru.ERROR_CHANNEL_PERMS
import pw.aru.utils.extensions.aru.ERROR_GUILD_PERMS
import pw.aru.utils.extensions.aru.onHelp
import pw.aru.utils.text.DISAPPOINTED
import pw.aru.utils.text.STOP
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2

class CommandProcessor(
    private val aru: Aru,
    private val db: AruDB,
    private val registry: CommandRegistry
) : KLogging() {

    private val perms = PermissionResolver(db)
    private val checks = CommandChecks(aru, db)

    var commandCount = 0

    fun onCommand(message: Message) {
        val raw = message.content()

        for (prefix in prefixes) {
            if (raw.startsWith(prefix)) {
                process(message, raw.substring(prefix.length).trimStart())
                return
            }
        }

        val guildPrefix: String? = runCatching {
            val settings = GuildSettings(db, message.guild()!!.idAsLong())
            when (aru) {
                MAIN -> settings.mainPrefix
                DEV -> settings.devPrefix
                PATREON -> settings.patreonPrefix
            }
        }.onFailure { logger.error("Error while Redis:", it) }.getOrNull()

        if (guildPrefix != null && raw.startsWith(guildPrefix)) {
            process(message, raw.substring(guildPrefix.length))
            return
        }

        // onDiscreteCommand(message)
        if (raw.startsWith('[') && raw.contains(']')) {
            val (cmdRaw, cmdOuter) = raw.substring(1).trimStart().split(']', limit = 2)

            for (prefix in prefixes) {
                if (cmdRaw.startsWith(prefix)) {
                    processDiscrete(message, cmdRaw.substring(prefix.length).trimStart(), cmdOuter)
                    return
                }
            }

            if (guildPrefix != null && cmdRaw.startsWith(guildPrefix)) {
                processDiscrete(message, cmdRaw.substring(guildPrefix.length), cmdOuter)
                return
            }
        }
    }

    val permissions = arrayOf(EMBED_LINKS, ATTACH_FILES, ADD_REACTIONS, USE_EXTERNAL_EMOJI)
    private fun checkAruPermissions(message: Message): Boolean {
        val self = message.guild()!!.selfMember()
        val channel = message.channel().asTextChannel()


        if (self.hasPermissions(channel, *permissions)) return true

        val guildCheck = self.hasPermissions(*permissions)
        val perms = permissions.map { it to self.hasPermissions(channel, it) }

        message.channel().sendMessage(
            arrayOf(
                "$STOP **Stop there!**",
                "I **require** the following permissions to work:",
                perms.joinToString("\n") { (perm, enabled) ->
                    "${if (enabled) "✅" else "❎"} **${perm.permName()}**"
                },
                "Sadly, I have to refuse all commands until you give me that permission. $DISAPPOINTED",
                "",
                if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                "If you need help on doing that, check my support server: `https://support.aru.pw/`"
            ).joinToString("\n")
        )

        return false
    }

    private fun process(message: Message, content: String) {
        if (!checkAruPermissions(message)) return

        val userPerms = perms.resolve(message.member()!!)
        if (!userPerms.contains(USE_BOT)) return // Global Blacklist

        val split = content.split(*SPLIT_CHARS, limit = 2)
        val cmd = split[0].toLowerCase()
        val args = split.getOrNull(1)?.trimStart(*SPLIT_CHARS) ?: ""

        val command = registry[cmd] ?: return processCustomCommand(message, cmd, args, userPerms)

        if (!checks.runChecks(message, command, userPerms)) return

        CommandStatsManager.log(cmd)

        runCommand(command, message, args, userPerms)

        logger.trace {
            "Command invoked: $cmd, by ${message.author().discordTag()} with timestamp ${Date()}"
        }
    }

    private fun processCustomCommand(message: Message, cmd: String, args: String, userPerms: Set<Permission>) {
        val ctx = CommandContext(message, args, userPerms)

        if (
            registry.lookup.keys.mapNotNull { it as? ICommand.CustomHandler }.any {
                it.runCatching { ctx.customCall(cmd) }.getOrNull() == ICommand.CustomHandler.Result.HANDLED
            }
        ) return

        // TODO: Implement?
    }

    private fun processDiscreteCustomCommand(
        message: Message,
        cmd: String,
        args: String,
        outer: String,
        userPerms: Set<Permission>
    ) {
        val ctx = CommandContext(message, args, userPerms)

        if (
            registry.lookup.keys.mapNotNull { it as? ICommand.CustomDiscreteHandler }.any {
                it.runCatching { ctx.customCall(cmd, outer) }.getOrNull() == ICommand.CustomHandler.Result.HANDLED
            }
        ) return

        // TODO: Implement?
    }

    private fun runCommand(command: ICommand, message: Message, args: String, userPerms: Set<Permission>) {
        commandCount++

        command.runCatching {
            CommandContext(message, args, userPerms).call()
        }.onFailure { runCatching { handleException(command, message, it) } }
    }

    private fun processDiscrete(message: Message, content: String, outer: String) {
        if (!checkAruPermissions(message)) return

        val userPerms = perms.resolve(message.member()!!)
        if (!userPerms.contains(USE_BOT)) return // Global Blacklist

        val split = content.split(' ', limit = 2)
        val cmd = split[0].toLowerCase()
        val args = split.getOrNull(1) ?: ""

        val command = registry[cmd] as? ICommand.Discrete ?: return processDiscreteCustomCommand(
            message,
            cmd,
            args,
            outer,
            userPerms
        )

        if (!checks.runChecks(message, command, userPerms)) return

        CommandStatsManager.log(cmd)

        runDiscreteCommand(command, message, args, outer, userPerms)

        logger.trace {
            "Discrete Command invoked: $cmd, by ${message.author().discordTag()} with timestamp ${Date()}"
        }
    }

    private fun runDiscreteCommand(
        command: ICommand.Discrete,
        message: Message,
        args: String,
        outer: String,
        userPerms: Set<Permission>
    ) {
        commandCount++

        command.runCatching {
            CommandContext(message, args, userPerms).discreteCall(outer)
        }.onFailure { runCatching { handleException(command, message, it) } }
    }

    private fun handleException(c: ICommand, message: Message, t: Throwable) {
        when {
            t == ShowHelp -> {
                onHelp(c, message)
            }

            c is ExceptionHandler -> {
                try {
                    c.handle(message, t)
                } catch (u: Exception) {
                    ErrorReporter()
                        .command(c)
                        .throwable(t)
                        .underlyingException(u)
                        .message(message)
                        .errorIdFromContext()
                        .appendMdc()
                        .report()
                        .logToFile()
                        .logAsError()
                        .sendErrorMessage()
                }
            }

            else -> {
                ErrorReporter()
                    .command(c)
                    .throwable(t)
                    .message(message)
                    .errorIdFromContext()
                    .report()
                    .logToFile()
                    .logAsError()
                    .sendErrorMessage()
            }
        }
    }

}