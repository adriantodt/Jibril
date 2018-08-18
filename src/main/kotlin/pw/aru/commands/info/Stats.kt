package pw.aru.commands.info

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.ISnowflake
import pw.aru.Aru
import pw.aru.core.CommandProcessor
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicManager
import pw.aru.exported.aru_version
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.emotes.LOADING
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.format
import pw.aru.utils.helpers.AsyncInfoMonitor.availableProcessors
import pw.aru.utils.helpers.AsyncInfoMonitor.cpuUsage
import pw.aru.utils.helpers.AsyncInfoMonitor.freeMemory
import pw.aru.utils.helpers.AsyncInfoMonitor.maxMemory
import pw.aru.utils.helpers.AsyncInfoMonitor.threadCount
import pw.aru.utils.helpers.AsyncInfoMonitor.totalMemory
import pw.aru.utils.helpers.AsyncInfoMonitor.vpsCpuUsage
import pw.aru.utils.helpers.AsyncInfoMonitor.vpsFreeMemory
import pw.aru.utils.helpers.AsyncInfoMonitor.vpsMaxMemory
import pw.aru.utils.helpers.AsyncInfoMonitor.vpsUsedMemory
import pw.aru.utils.helpers.CommandStatsManager
import pw.aru.utils.helpers.GuildStatsManager
import pw.aru.utils.helpers.StatsManager
import pw.aru.utils.helpers.StatsManager.Type
import pw.aru.utils.helpers.StatsManager.Type.*
import java.lang.Thread.sleep

@Command("stats")
@UseFullInjector
class Stats
(
    private val shardManager: ShardManager,
    private val musicManager: MusicManager,
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
        EmbedFirst(event) {
            baseEmbed(event, "Aru! | Discord Stats")
            field("Uptime:", Aru.uptime)
            field("Bot Stats:",
                arrayOf(
                    "\u25AB **Aru Version**: $aru_version",
                    "\u25AB **Threads**: ${Thread.activeCount().format("%,d")}",
                    "\u25AB **Shards**: ${shardManager.shardsTotal.format("%,d")} (Current: ${event.jda.shardInfo.shardId})",
                    "\u25AB **Commands**: ${processor.commandCount.format("%,d")} executed"
                ),
                inline = true
            )
            field("Discord Stats:", "$LOADING Gathering $LOADING", inline = true)
        } then {
            sleep(100)

            val guildCount = shardManager.guildCache.size().format("%,d")
            val uniqueUserCount = shardManager.userCache.stream().map(ISnowflake::getIdLong).distinct().count().format("%,d")
            val textChannelCount = shardManager.textChannelCache.size().format("%,d")
            val voiceChannelCount = shardManager.voiceChannelCache.size().format("%,d")
            val musicCount = musicManager.musicPlayers.size().format("%,d")
            val queueSize = musicManager.musicPlayers.valueCollection()
                .map { it.queue.size }
                .sum().format("%,d")

            fields.remove(fields.last())
            field("Discord Stats:",
                arrayOf(
                    "\u25AB **Servers**: $guildCount",
                    "\u25AB **Unique Users**: $uniqueUserCount",
                    "\u25AB **Text Channels**: $textChannelCount",
                    "\u25AB **Voice Channels**: $voiceChannelCount",
                    "\u25AB **Playing music on $musicCount servers**",
                    "\u25AB **$queueSize tracks queued**"
                ),
                inline = true
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
            baseEmbed(event, title)
            arrayOf(MINUTE, HOUR, DAY, TOTAL).forEach {
                field(it.name, m.resume(it))
            }
        }.queue()
    }

    private fun <T> CommandContext.detailedStatsManager(m: StatsManager<T>, title: String, type: Type) {
        sendEmbed {
            baseEmbed(event, "$title | ${type.display}")
            m.fillEmbed(this, type)
        }.queue()
    }

    private fun CommandContext.serverStats() {
        sendEmbed {
            baseEmbed(event, "Aru! | Server Stats")
            field(
                "Resource Usage:",
                arrayOf(
                    "\u25AB **Threads**: $threadCount",
                    "\u25AB **RAM**: ${(totalMemory - freeMemory).format("%.2f")}MB/${maxMemory.format("%.2f")}MB",
                    "\u25AB **Allocated Memory**: ${totalMemory.format("%.2f")}MB (${freeMemory.format("%.2f")}MB remaining)",
                    "\u25AB **CPU Usage**: ${cpuUsage.format("%.2f")}%"
                )
            )
            field(
                "Server:",
                arrayOf(
                    "\u25AB **RAM** (Total/Free/Used): ${vpsMaxMemory.format("%.2f")}GB/${vpsFreeMemory.format("%.2f")}GB/${vpsUsedMemory.format("%.2f")}GB",
                    "\u25AB **CPU Cores**: ${availableProcessors.format("%.2f")} cores",
                    "\u25AB **CPU Usage**: ${vpsCpuUsage.format("%.2f")}%"
                )
            )
        }.queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("stats"), "Stats Command"),
        Description("Shows this bot's stats."),
        Usage(
            CommandUsage("stats [discord/d]", "Shows my general stats."),
            CommandUsage("stats <server/s>", "Shows my server's stats."),
            CommandUsage("stats <cmds/cmd/commands/c>", "Shows this session's commands stats."),
            CommandUsage("stats <cmds/cmd/commands/c> <now/hourly/dialy/total>", "Shows detailed info about command usage."),
            CommandUsage("stats <guilds/guild/g>", "Shows this session's guild join/leave stats."),
            CommandUsage("stats <guilds/guild/g> <now/hourly/dialy/total>", "Shows detailed info about guild join/leave events.")
        )
    )
}
