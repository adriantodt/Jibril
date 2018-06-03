package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.set.hash.TIntHashSet
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils.replaceEach
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import java.util.concurrent.LinkedBlockingDeque

@Command("removetrack", "removesong")
class RemoveTrack(musicManager: MusicManager) : MusicPermissionCommand(musicManager, "voteshuffle"), ICommand.HelpDialogProvider {
    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val list = musicPlayer.queue.toList()

        val selected = TIntHashSet()

        val last = list.size.toString()

        for (param in args.split(' ')) {
            val arg = replaceEach(
                param,
                arrayOf("first", "next", "last", "all"),
                arrayOf("1", "1", last, "0-$last")
            )

            if (arg.contains("-") || arg.contains("~")) {
                val range = args.split("[-~]")

                if (range.size != 2) {
                    event.channel.sendMessage("$ERROR ``$param`` is not a valid range!").queue()
                    return
                }

                try {
                    val iStart = range[0].toInt() - 1
                    val iEnd = range[1].toInt() - 1

                    if (iStart < 0 || iStart >= list.size) {
                        event.channel.sendMessage("$ERROR There isn't a queued track at the position ``$iStart``!").queue()
                        return
                    }

                    if (iEnd < 0 || iEnd >= list.size) {
                        event.channel.sendMessage("$ERROR There isn't a queued track at the position ``$iEnd``!").queue()
                        return
                    }

                    (iStart..iEnd).forEach { selected.add(it) }
                } catch (ex: NumberFormatException) {
                    event.channel.sendMessage("$ERROR ``$param`` is not a valid range!").queue()
                    return
                }

            } else {
                try {
                    val i = Integer.parseInt(args) - 1

                    if (i < 0 || i >= list.size) {
                        event.channel.sendMessage("$ERROR There isn't a queued track at the position ``$i``!").queue()
                        return
                    }

                    selected.add(i)
                } catch (ex: NumberFormatException) {
                    event.channel.sendMessage("$ERROR ``$arg`` is not a valid number or range!").queue()
                    return
                }
            }
        }

        musicPlayer.queue = list.filterIndexedTo(LinkedBlockingDeque()) { index, _ -> !selected.contains(index) }

        event.channel.sendMessage("$SUCCESS Removed **${selected.size()}** track(s) from the queue.").queue()
    }

    override val helpHandler = HelpFactory("RemoveTrack Command") {
        aliases("removesong")

        description("Remove the specified track from the queue.")

        usage("removetrack first", "Remove the first track.")
        usage("removetrack last", "Remove the first track.")
        usage("removetrack <track number>", "Remove the specified track.")
        usage("removetrack <from~to>", "Remove the tracks in a specific range.")
    }
}