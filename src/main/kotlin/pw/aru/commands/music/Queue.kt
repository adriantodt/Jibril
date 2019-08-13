package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.utils.NowPlayingEmbed.musicLength
import pw.aru.bot.music.utils.NowPlayingEmbed.progressBar
import pw.aru.commands.music.base.MusicCommand
import pw.aru.utils.extensions.lib.blankField
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling
import pw.aru.utils.text.PLAY
import pw.aru.utils.text.STREAMING

@Command("queue", "q")
class Queue(musicSystem: MusicSystem) : MusicCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.call(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        val queue = musicPlayer.queue
        val lastPage = maxOf(1, queue.size / 5)
        val page = (if (args.isEmpty()) 1 else args.toIntOrNull() ?: return showHelp())
            .coerceIn(1, lastPage)
            .minus(1)

        sendEmbed {
            styling(message)
                .author("Queue for guild ${guild.name()}", image = guild.iconUrl())
                .applyAll()

            field(
                "Now Playing:",
                "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                "",
                if (currentTrack.duration == Long.MAX_VALUE)
                    "$PLAY Streaming... $STREAMING"
                else
                    "$PLAY ${progressBar(
                        musicPlayer.lastPosition,
                        currentTrack.duration
                    )} (`${musicLength(currentTrack.duration - musicPlayer.lastPosition)}`)"
            )

            thumbnail(musicPlayer.lastTrackData!!.thumbnail)

            blankField()

            field("Queue: (${queue.size} songs - ${musicLength(queue)})",
                if (queue.isEmpty()) {
                    "Music queue is empty! You can add more songs using `$prefix${"play"}`!"
                } else {
                    queue.withIndex()
                        .drop(page * 5)
                        .take(5)
                        .joinToString("\n") { (index, t) ->
                            "**#${index + 1} [${t.info.title}](${t.info.uri})** - (`${musicLength(t.info.length)}`)"
                        }
                }
            )

            footer(
                "Page ${page + 1} of $lastPage | Requested by ${author.effectiveName()}",
                author.effectiveAvatarUrl()
            )
        }
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("queue", "q"),
            "Queue Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description("Displays the current queue."),
        SeeAlso["play", "nowplaying", "repeat"]
    )
}