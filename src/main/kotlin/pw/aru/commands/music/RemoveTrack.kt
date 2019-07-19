package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.utils.extensions.lang.replaceEach
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.SUCCESS
import java.util.concurrent.LinkedBlockingDeque

@Command("removetrack", "removesong")
class RemoveTrack(musicSystem: MusicSystem) : MusicPermissionCommand(musicSystem, "voteshuffle"),
    ICommand.HelpDialogProvider {
    override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        val list = musicPlayer.queue.toList()

        val selected = HashSet<Int>()

        val last = list.size.toString()

        for (param in args.split(' ')) {
            val arg = param.replaceEach(
                "first" to "1",
                "next" to "1",
                "last" to last
            )

            if (arg == "all") {
                (0..list.size).forEach { selected.add(it) }
                break
            } else if (arg.contains("-") || arg.contains("~")) {
                val range = args.split("[-~]")

                if (range.size != 2) {
                    send("$ERROR ``$param`` is not a valid range!")
                    return
                }

                try {
                    val iStart = range[0].toInt() - 1
                    val iEnd = range[1].toInt() - 1

                    if (iStart < 0 || iStart >= list.size) {
                        send("$ERROR There isn't a queued track at the position ``$iStart``!")
                        return
                    }

                    if (iEnd < 0 || iEnd >= list.size) {
                        send("$ERROR There isn't a queued track at the position ``$iEnd``!")
                        return
                    }

                    (iStart..iEnd).forEach { selected.add(it) }
                } catch (ex: NumberFormatException) {
                    send("$ERROR ``$param`` is not a valid range!")
                    return
                }

            } else {
                try {
                    val i = Integer.parseInt(args) - 1

                    if (i < 0 || i >= list.size) {
                        send("$ERROR There isn't a queued track at the position ``$i``!")
                        return
                    }

                    selected.add(i)
                } catch (ex: NumberFormatException) {
                    send("$ERROR ``$arg`` is not a valid number or range!")
                    return
                }
            }
        }

        musicPlayer.queue = list.filterIndexedTo(LinkedBlockingDeque()) { index, _ -> !selected.contains(index) }

        send("$SUCCESS Removed **${selected.size}** track(s) from the queue.")
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("removetrack", "removesong"),
            "RemoveTrack Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description("Remove the specified track from the queue."),
        Usage(
            CommandUsage("removetrack first", "Remove the first track."),
            CommandUsage("removetrack last", "Remove the last track."),
            CommandUsage("removetrack <track number>", "Remove the specified track."),
            CommandUsage("removetrack <from~to>", "Remove the tracks in a specific range."),
            CommandUsage("removetrack all", "Remove all the tracks of the queue.")
        )
    )
}