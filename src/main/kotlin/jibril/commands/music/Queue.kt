package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.core.music.musicLength
import jibril.core.music.progressBar
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.LOADING
import jibril.utils.emotes.PLAY
import jibril.utils.extensions.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

@Command("queue", "q")
class Queue
@Inject constructor(
    musicManager: MusicManager
) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val queue = musicPlayer.queue
        val lastPage = queue.size / 5
        val page = (if (args.isEmpty()) 1 else args.toIntOrNull() ?: return showHelp())
            .minus(1)
            .coerceIn(0, lastPage)

        embed {
            baseEmbed(event, "Queue for guild ${event.guild.name}", image = event.guild.iconUrl)

            field(
                "Now Playing:",
                if (currentTrack.duration == Long.MAX_VALUE) {
                    arrayOf(
                        "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                        "",
                        "$PLAY $LOADING Streaming $LOADING"
                    )
                } else {
                    arrayOf(
                        "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                        "",
                        "$PLAY ${progressBar(currentTrack.position, currentTrack.duration)} (`${musicLength(currentTrack.duration - currentTrack.position)}`)"
                    )
                }
            )

            thumbnail(musicManager.resolveThumbnail(currentTrack))

            addBlankField(false)

            field("Queue: (${queue.size} songs - ${musicLength(queue)})",
                if (queue.isEmpty()) {
                    "Music queue is empty! You can add more songs using `j!play`!"
                } else {
                    queue.withIndex()
                        .drop(page * 5)
                        .take(5)
                        .joinToString("\n") { (index, t) ->
                            "**#${index + 1} [${t.info.title}](${t.info.uri})** - (`${musicLength(t.info.length)}`)"
                        }
                }
            )

            footer("Page ${page.plus(1)} of ${lastPage.plus(1)} | Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)
        }.send(event).queue()
    }

    override val helpHandler = HelpFactory("Queue Command") {
        aliases("q")
        description("Displays the current queue.")
    }
}