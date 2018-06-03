package pw.aru.commands.info

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.ISnowflake
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.Aru
import pw.aru.core.CommandProcessor
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.SimpleArgsCommand
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.EmbedFirst
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.LOADING
import pw.aru.utils.extensions.*
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
class Stats
(
    private val shardManager: ShardManager,
    private val musicManager: MusicManager
) : SimpleArgsCommand(expectedArgs = 2), ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: Array<String>) {
        when (args.getOrNull(0)) {
            "server", "s" -> serverStats(event)
            null, "discord", "d" -> discordStats(event)
            "cmds", "cmd", "commands", "c" -> statsManager(CommandStatsManager, "Command Stats", event, args.getOrNull(1))
            "guilds", "guild", "g" -> statsManager(GuildStatsManager, "Guild Stats", event, args.getOrNull(1))
            else -> showHelp()
        }
    }

    private fun discordStats(event: GuildMessageReceivedEvent) {
        EmbedFirst(event) {
            baseEmbed(event, "Aru Bot | Discord Stats")
            field("Uptime:", Aru.uptime)
            field("Bot Stats:",
                arrayOf(
                    "\u25AB **Aru Version**: ${Aru.version}",
                    "\u25AB **Threads**: ${Thread.activeCount().format("%,d")}",
                    "\u25AB **Shards**: ${shardManager.shardsTotal.format("%,d")} (Current: ${event.jda.shardInfo.shardId})",
                    "\u25AB **Commands**: ${CommandProcessor.commandCount.format("%,d")} executed"
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

    private fun <T> statsManager(m: StatsManager<T>, title: String, event: GuildMessageReceivedEvent, arg: String?) {
        when (arg) {
            null -> statsManagerResume(m, title, event)
            "total", "t" -> detailedStatsManager(m, title, event, "Total", TOTAL)
            "daily", "d", "dialy", "day" -> detailedStatsManager(m, title, event, "Today", DAY)
            "hourly", "h", "hour" -> detailedStatsManager(m, title, event, "This Hour", HOUR)
            "now", "n", "minute", "min", "m" -> detailedStatsManager(m, title, event, "Now", MINUTE)
            else -> showHelp()
        }
    }

    private fun <T> statsManagerResume(m: StatsManager<T>, title: String, event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, title)
            addField("Now", m.resume(MINUTE), false)
            addField("This Hour", m.resume(HOUR), false)
            addField("Today", m.resume(DAY), false)
            addField("Total", m.resume(TOTAL), false)
        }.send(event).queue()
    }

    private fun <T> detailedStatsManager(m: StatsManager<T>, title: String, event: GuildMessageReceivedEvent, display: String, type: Type) {
        embed {
            baseEmbed(event, "$title | $display")
            m.fillEmbed(this, type)
        }.send(event).queue()
    }

    private fun serverStats(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, "Aru Bot | Server Stats")
            field(
                "Resource Usage:",
                arrayOf(
                    "\u25AB **Threads**: $threadCount",
                    "\u25AB **RAM**: ${totalMemory - freeMemory}MB/${maxMemory}MB",
                    "\u25AB **Allocated Memory**: ${totalMemory}MB (${freeMemory}MB remaining)",
                    "\u25AB **CPU Usage**: $cpuUsage%"
                )
            )
            field(
                "Server:",
                arrayOf(
                    "\u25AB **RAM** (Total/Free/Used): ${vpsMaxMemory}GB/${vpsFreeMemory}GB/${vpsUsedMemory}GB",
                    "\u25AB **CPU Cores**: $availableProcessors cores",
                    "\u25AB **CPU Usage**: $vpsCpuUsage%"
                )
            )
        }.send(event).queue()
    }

    override val helpHandler = HelpFactory("Stats Command") {
        description("Shows this bot's stats.")
        usage("stats [discord/d]", "Shows my general stats.")
        usage("stats <server/s>", "Shows my server's stats.")
        usage("stats <cmds/cmd/commands/c>", "Shows this session's commands stats.")
        usage("stats <cmds/cmd/commands/c> <now/hourly/dialy/total>", "Shows detailed info about command usage.")
        usage("stats <guilds/guild/g>", "Shows this session's guild join/leave stats.")
        usage("stats<guilds/guild/g> <now/hourly/dialy/total>", "Shows detailed info about guild join/leave events.")
    }
}
