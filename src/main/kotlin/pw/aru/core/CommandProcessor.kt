package pw.aru.core

import mu.KLogging
import net.dv8tion.jda.core.Permission.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.Aru.prefixes
import pw.aru.core.commands.ICommand
import pw.aru.db.AruDB
import pw.aru.db.entities.GuildSettings
import pw.aru.snow64.Snow64
import pw.aru.utils.emotes.*
import pw.aru.utils.extensions.*
import pw.aru.utils.helpers.CommandStatsManager
import redis.clients.jedis.exceptions.JedisConnectionException
import java.util.*

class CommandProcessor(private val db: AruDB, private val registry: CommandRegistry) : KLogging() {

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
            GuildSettings(db, event.guild.idLong).prefix
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

    val permissions = arrayOf(MESSAGE_EMBED_LINKS, MESSAGE_ADD_REACTION, MESSAGE_EXT_EMOJI)
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
                perms.joinToString("\n") { (perm, enabled) -> "${if (enabled) "✅" else "❎"} **${perm.name}**" },
                "Sadly, I have to refuse all commands until you give me that permission. $DISAPPOINTED",
                "",
                if (guildCheck) ERROR_CHANNEL_PERMS else ERROR_GUILD_PERMS,
                "If you need help on doing that, check my support server: ``https://support.aru.pw/``"
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

        if (command is ICommand.Permission && !command.permission.test(event.member)) {
            event.channel.sendMessage("$STOP B-baka, I'm not allowed to let you do that!").queue()
            return
        }

        CommandStatsManager.log(cmd)

        runCommand(command, event, args)

        logger.trace {
            "Command invoked: $cmd, by ${event.author.name}#${event.author.discriminator} with timestamp ${Date()}"
        }
    }

    private fun processCustomCommand(event: GuildMessageReceivedEvent, cmd: String, args: String) {
        // TODO: Implement?
    }

    private fun runCommand(command: ICommand, event: GuildMessageReceivedEvent, args: String) {
        commandCount++
        try {
            command.call(event, args)
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

        val command = registry[cmd] as? ICommand.Discrete ?: return

        if (command is ICommand.Permission && !command.permission.test(event.member)) {
            event.channel.sendMessage("$STOP B-baka, I'm not allowed to let you do that!").queue()
            return
        }

        CommandStatsManager.log(cmd)

        runDiscreteCommand(command, event, args, outer)

        logger.trace {
            "Discrete Command invoked: $cmd, by ${event.author.name}#${event.author.discriminator} with timestamp ${Date()}"
        }
    }

    private fun runDiscreteCommand(command: ICommand.Discrete, event: GuildMessageReceivedEvent, args: String, outer: String) {
        commandCount++
        try {
            command.discreteCall(event, args, outer)
        } catch (e: Exception) {
            try {
                handleException(command, event, e)
            } catch (_: Exception) {
            }
        }

    }

    private fun handleException(c: ICommand, event: GuildMessageReceivedEvent, e: Exception) {
        if (e == CommandExceptions.ShowHelp) {
            return onHelp(c, event)
        }

        if (c is ICommand.ExceptionHandler) {
            try {
                c.handle(event, e)
            } catch (e2: Exception) {
                reportException(c, event, e, if (e != e2) e2 else null)
            }
            return
        }

        reportException(c, event, e)
    }

    private val errorQuotes = listOf(
        "What is happening? I'm sorry, I'm sorry, I'm sorry!",
        "Wha? Everything caught fire! qwq",
        "What am I supposed to do with an error? Because I got one."
    )

    private fun reportException(c: ICommand, event: GuildMessageReceivedEvent, e: Exception, h: Exception? = null) {
        val errorId = J.initials(J.simpleName(e)) + "#" + Snow64.fromSnowflake(event.message.idLong)

        logger.error(e) {
            "**ERROR REPORTED**\n**ErrorID**: `$errorId`\n**Type**: `${e.javaClass.simpleName}`\n**Command**: ``${event.message.contentRaw}`\n"
        }

        if (h != null) logger.error(h) {
            "(ErrorID `$errorId`'s underlying exception)\nType: ``${h.javaClass.simpleName}``\n"
        }

        //when (e) {
        //    is SyntaxException -> {
        //        event.channel.sendMessage(
        //            "$ERROR S-sorry, the page caught fire. Please, forgive me!\n(ErrorID: `$errorId`)"
        //        ).queue()
        //    }
        //    is ReqlError -> {
        //        event.channel.sendMessage(
        //            "$ERROR W-wha? I think I lost my notebook. W-we're still friends, right?\n(ErrorID: `$errorId`)"
        //        ).queue()
        //    }
        //    else -> {
        event.channel.sendMessage(
            "$WORRIED ${errorQuotes.random()}\n\n$TALKING Eh, do you mind reporting this to my developers? (Check out `${"hangout".withPrefix()}`)\n$PENCIL **Error ID**: `$errorId`"
        ).queue()
        //    }
        //}
    }
}