package pw.aru.core

import mu.KLogging
import net.dv8tion.jda.core.Permission.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.Aru
import pw.aru.Aru.*
import pw.aru.Aru.Bot.prefixes
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.ExceptionHandler
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.context.CommandContext.ShowHelp
import pw.aru.core.reporting.ErrorReporter
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.utils.emotes.DISAPPOINTED
import pw.aru.utils.emotes.STOP
import pw.aru.utils.extensions.ERROR_CHANNEL_PERMS
import pw.aru.utils.extensions.ERROR_GUILD_PERMS
import pw.aru.utils.extensions.onHelp
import pw.aru.utils.helpers.CommandStatsManager
import redis.clients.jedis.exceptions.JedisConnectionException
import java.util.*

class CommandProcessor(private val aru: Aru, private val db: AruDB, private val registry: CommandRegistry) : KLogging() {

    private val checks = CommandChecks(aru, db)

    var commandCount = 0

    fun onCommand(event: GuildMessageReceivedEvent) {
        val raw = event.message.contentRaw

        for (prefix in prefixes) {
            if (raw.startsWith(prefix)) {
                process(event, raw.substring(prefix.length).trimStart())
                return
            }
        }

        val guildPrefix = try {
            val settings = GuildSettings(db, event.guild.idLong)
            when (aru) {
                MAIN -> settings.mainPrefix
                DEV -> settings.devPrefix
                PATREON -> settings.patreonPrefix
            }
        } catch (_: JedisConnectionException) {
            null
        }

        if (guildPrefix != null && raw.startsWith(guildPrefix)) {
            process(event, raw.substring(guildPrefix.length))
            return
        }

        // onDiscreteCommand(event)
        if (raw.startsWith('[') && raw.contains(']')) {
            val (cmdRaw, cmdOuter) = raw.substring(1).trimStart().split(']', limit = 2)

            for (prefix in prefixes) {
                if (cmdRaw.startsWith(prefix)) {
                    processDiscrete(event, cmdRaw.substring(prefix.length).trimStart(), cmdOuter)
                    return
                }
            }

            if (guildPrefix != null && cmdRaw.startsWith(guildPrefix)) {
                processDiscrete(event, cmdRaw.substring(guildPrefix.length), cmdOuter)
                return
            }
        }
    }

    val permissions = arrayOf(MESSAGE_EMBED_LINKS, MESSAGE_ATTACH_FILES, MESSAGE_ADD_REACTION, MESSAGE_EXT_EMOJI)
    private fun checkPermissions(event: GuildMessageReceivedEvent): Boolean {
        val self = event.guild.selfMember
        val channel = event.channel

        if (self.hasPermission(channel, *permissions)) return true

        val guildCheck = self.hasPermission(*permissions)
        val perms = permissions.map { it to self.hasPermission(channel, it) }

        event.channel.sendMessage(
            arrayOf(
                "$STOP **Stop there!**",
                "I **require** the following permissions to work:",
                perms.joinToString("\n") { (perm, enabled) -> "${if (enabled) "✅" else "❎"} **${perm.getName()}**" },
                "Sadly, I have to refuse all commands until you give me that permission. $DISAPPOINTED",
                "",
                if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                "If you need help on doing that, check my support server: `https://support.aru.pw/`"
            ).joinToString("\n")
        ).queue()

        return false
    }

    private fun process(event: GuildMessageReceivedEvent, content: String) {
        if (!checkPermissions(event)) return

        val split = content.split(' ', limit = 2)
        val cmd = split[0].toLowerCase()
        val args = split.getOrNull(1) ?: ""

        val command = registry[cmd] ?: return processCustomCommand(event, cmd, args)

        if (!checks.runChecks(event, command)) return

        CommandStatsManager.log(cmd)

        runCommand(command, event, args)

        logger.trace {
            "Command invoked: $cmd, by ${event.author.name}#${event.author.discriminator} with timestamp ${Date()}"
        }
    }

    private fun processCustomCommand(message: GuildMessageReceivedEvent, cmd: String, args: String) {
        val ctx = CommandContext(message, args)

        if (
            registry.lookup.keys.mapNotNull { it as? ICommand.CustomHandler }.any {
                it.runCatching { ctx.customCall(cmd) }.getOrNull() == ICommand.CustomHandler.Result.HANDLED
            }
        ) return

        // TODO: Implement?
    }

    private fun processDiscreteCustomCommand(
        message: GuildMessageReceivedEvent,
        cmd: String,
        args: String,
        outer: String
    ) {
        val ctx = CommandContext(message, args)

        if (
            registry.lookup.keys.mapNotNull { it as? ICommand.CustomDiscreteHandler }.any {
                it.runCatching { ctx.customCall(cmd, outer) }.getOrNull() == ICommand.CustomHandler.Result.HANDLED
            }
        ) return

        // TODO: Implement?
    }

    private fun runCommand(command: ICommand, event: GuildMessageReceivedEvent, args: String) {
        commandCount++
        try {
            with(command) {
                CommandContext(event, args).call()
            }
        } catch (e: Exception) {
            try {
                handleException(command, event, e)
            } catch (_: Exception) {
            }
        }

    }

    private fun processDiscrete(event: GuildMessageReceivedEvent, content: String, outer: String) {
        if (!checkPermissions(event)) return

        val split = content.split(' ', limit = 2)
        val cmd = split[0].toLowerCase()
        val args = split.getOrNull(1) ?: ""

        val command =
            registry[cmd] as? ICommand.Discrete ?: return processDiscreteCustomCommand(event, cmd, args, outer)

        if (!checks.runChecks(event, command)) return

        CommandStatsManager.log(cmd)

        runDiscreteCommand(command, event, args, outer)

        logger.trace {
            "Discrete Command invoked: $cmd, by ${event.author.name}#${event.author.discriminator} with timestamp ${Date()}"
        }
    }

    private fun runDiscreteCommand(command: ICommand.Discrete, event: GuildMessageReceivedEvent, args: String, outer: String) {
        commandCount++
        try {
            with(command) {
                CommandContext(event, args).discreteCall(outer)
            }
        } catch (e: Exception) {
            try {
                handleException(command, event, e)
            } catch (_: Exception) {
            }
        }
    }

    private fun handleException(c: ICommand, event: GuildMessageReceivedEvent, e: Exception) {
        when {
            e == ShowHelp -> {
                onHelp(c, event)
            }

            c is ExceptionHandler -> {
                try {
                    c.handle(event, e)
                } catch (u: Exception) {
                    ErrorReporter()
                        .command(c)
                        .exception(e)
                        .underlyingException(u)
                        .message(event)
                        .errorIdFromContext()
                        .report()
                        .logToFile()
                        .logAsError()
                        .sendErrorMessage()
                }
            }

            else -> {
                ErrorReporter()
                    .command(c)
                    .exception(e)
                    .message(event)
                    .errorIdFromContext()
                    .report()
                    .logToFile()
                    .logAsError()
                    .sendErrorMessage()
            }
        }
    }
}