package pw.aru.commands.info

import com.mewna.catnip.entity.channel.Channel.ChannelType.*
import pw.aru.Aru.Bot.uptime
import pw.aru.bot.CommandProcessor
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicSystem
import pw.aru.exported.aru_version
import pw.aru.utils.*
import pw.aru.utils.AsyncInfoMonitor.availableProcessors
import pw.aru.utils.AsyncInfoMonitor.cpuUsage
import pw.aru.utils.AsyncInfoMonitor.freeMemory
import pw.aru.utils.AsyncInfoMonitor.maxMemory
import pw.aru.utils.AsyncInfoMonitor.threadCount
import pw.aru.utils.AsyncInfoMonitor.totalMemory
import pw.aru.utils.AsyncInfoMonitor.vpsCpuUsage
import pw.aru.utils.AsyncInfoMonitor.vpsFreeMemory
import pw.aru.utils.AsyncInfoMonitor.vpsMaxMemory
import pw.aru.utils.AsyncInfoMonitor.vpsUsedMemory
import pw.aru.utils.StatsManager.Type
import pw.aru.utils.StatsManager.Type.*
import pw.aru.utils.extensions.lang.format
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.extensions.lib.inlineField
import pw.aru.utils.text.LOADING

@Command("stats")
class Stats
    (
    private val musicSystem: MusicSystem,
    private val processor: CommandProcessor
) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "server", "s" -> serverStats()
            "", "discord", "d" -> discordStats()
            "cmds", "cmd", "commands", "c" -> statsManager(CommandStatsManager, "Command Stats", args.takeString())
            "guilds", "guild", "g" -> statsManager(GuildStatsManager, "Guild Stats", args.takeString())
            else -> showHelp()
        }
    }

    private fun CommandContext.discordStats() {

        EmbedFirst(message) {
            styling(message).author("Aru! | Discord Stats").applyAll()
            field("Uptime:", uptime)
            inlineField(
                "Bot Stats:",
                "\u25AB **Aru Version**: $aru_version",
                "\u25AB **Threads**: ${threadCount.format("%,d")}",
                "\u25AB **Shards**: ${catnip.shardManager().shardCount().format("%,d")}",
                "\u25AB **Commands**: ${processor.commandCount.format("%,d")} executed"
            )
            inlineField("Discord Stats:", "$LOADING Gathering $LOADING")
        } then {
            val guildCount = catnip.cache().guilds().size().format("%,d")
            val userCount = catnip.cache().users().size().format("%,d")
            val channelCounts = catnip.cache().channels().groupingBy { it.type() }.eachCount()
            val musicCount = musicSystem.players.size.format("%,d")
            val queueSize = musicSystem.players.values.asSequence()
                .map { it.queue.size }.sum().format("%,d")

            replaceAtIndex(
                2,
                "Discord Stats:",
                multiline(
                    "\u25AB **Servers**: $guildCount",
                    "\u25AB **Users**: $userCount",
                    "\u25AB **Text Channels**: ${channelCounts[TEXT]?.format("%,d")}",
                    "\u25AB **Voice Channels**: ${channelCounts[VOICE]?.format("%,d")}",
                    "\u25AB **Categories**: ${channelCounts[CATEGORY]?.format("%,d")}",
                    "\u25AB **Playing music on $musicCount servers**",
                    "\u25AB **$queueSize tracks queued**"
                ),
                true
            )
        }

    }

    private fun <T> CommandContext.statsManager(m: StatsManager<T>, title: String, arg: String) {
        when (arg) {
            "" -> statsManagerResume(m, title)
            "total", "t" -> detailedStatsManager(m, title, TOTAL)
            "daily", "d", "dialy", "day" -> detailedStatsManager(m, title, DAY)
            "hourly", "h", "hour" -> detailedStatsManager(m, title, HOUR)
            "now", "n", "minute", "min", "m" -> detailedStatsManager(m, title, MINUTE)
            else -> showHelp()
        }
    }

    private fun <T> CommandContext.statsManagerResume(m: StatsManager<T>, title: String) {
        sendEmbed {
            styling(message).author(title).applyAll()
            arrayOf(MINUTE, HOUR, DAY, TOTAL).forEach {
                field(it.name, m.takeSnapshot(it).resume())
            }
        }
    }

    private fun <T> CommandContext.detailedStatsManager(m: StatsManager<T>, title: String, type: Type) {
        sendEmbed {
            styling(message).author("$title | ${type.display}").applyAll()

            val (sum, items) = m.takeSnapshot(type)

            if (sum == 0L) {
                field("Nothing Here.", "Just Dust.", false)
            } else {
                description(
                    "Total: $sum\n" + items.entries.asSequence()
                        .map { it.key to it.value.get() }
                        .filter { it.second > 0 }
                        .sortedByDescending(Pair<T, Long>::second)
                        .take(20)
                        .joinToString("\n") { (k, v) ->
                            val p = Math.round(v * 100.0f / sum)
                            "${StatsManager.bar(p, 5)} **$k** - $p% ($v)"
                        }
                )
            }
        }
    }

    private fun CommandContext.serverStats() {
        sendEmbed {
            styling(message).author("Aru! | Server Stats").applyAll()
            field(
                "Resource Usage:",
                "\u25AB **Threads**: $threadCount",
                "\u25AB **RAM**: ${(totalMemory - freeMemory).format("%.2f")}MB/${maxMemory.format("%.2f")}MB",
                "\u25AB **Allocated Memory**: ${totalMemory.format("%.2f")}MB (${freeMemory.format("%.2f")}MB remaining)",
                "\u25AB **CPU Usage**: ${cpuUsage.format("%.2f")}%"
            )
            field(
                "Server:",
                "\u25AB **RAM** (Total/Free/Used): ${vpsMaxMemory.format("%.2f")}GB/${vpsFreeMemory.format("%.2f")}GB/${vpsUsedMemory.format(
                    "%.2f"
                )}GB",
                "\u25AB **CPU Cores**: $availableProcessors cores",
                "\u25AB **CPU Usage**: ${vpsCpuUsage.format("%.2f")}%"
            )
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("stats"), "Stats Command"),
        Description("Shows this bot's stats."),
        Usage(
            CommandUsage("stats [discord/d]", "Shows my general stats."),
            CommandUsage("stats <server/s>", "Shows my server's stats."),
            CommandUsage("stats <cmds/cmd/commands/c>", "Shows this session's commands stats."),
            CommandUsage(
                "stats <cmds/cmd/commands/c> <now/hourly/dialy/total>",
                "Shows detailed info about command usage."
            ),
            CommandUsage("stats <guilds/guild/g>", "Shows this session's guild join/leave stats."),
            CommandUsage(
                "stats <guilds/guild/g> <now/hourly/dialy/total>",
                "Shows detailed info about guild join/leave events."
            )
        )
    )
}
