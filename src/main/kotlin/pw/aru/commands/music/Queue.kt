package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.core.music.musicLength
import pw.aru.core.music.progressBar
import pw.aru.utils.emotes.LOADING
import pw.aru.utils.emotes.PLAY
import pw.aru.utils.extensions.baseEmbed
import pw.aru.utils.extensions.field
import pw.aru.utils.extensions.footer
import pw.aru.utils.extensions.thumbnail

@Command("queue", "q")
@UseFullInjector
class Queue(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        val queue = musicPlayer.queue
        val lastPage = maxOf(1, queue.size / 5)
        val page = (if (args.isEmpty()) 1 else args.toIntOrNull() ?: return showHelp())
            .coerceIn(1, lastPage)
            .minus(1)

        sendEmbed {
            baseEmbed(event, "Queue for guild ${event.guild.name}", image = event.guild.iconUrl)

            field(
                "Now Playing:",
                "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                "",
                if (currentTrack.duration == Long.MAX_VALUE)
                    "$PLAY $LOADING Streaming $LOADING"
                else
                    "$PLAY ${progressBar(currentTrack.position, currentTrack.duration)} (`${musicLength(currentTrack.duration - currentTrack.position)}`)"
            )

            thumbnail(musicManager.resolveThumbnail(currentTrack))

            addBlankField(false)

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

            footer("Page ${page + 1} of $lastPage | Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)
        }.queue()
    }

    override val helpHandler = Help(
        CommandDescription(listOf("queue", "q"), "Queue Command"),
        Description("Displays the current queue."),
        SeeAlso["play", "nowplaying", "repeat"]
    )
}