package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.*
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.LOADING
import jibril.utils.emotes.PLAY
import jibril.utils.extensions.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

@Command("nowplaying", "np")
class NowPlaying
@Inject constructor(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val queue = musicPlayer.queue
        val info = currentTrack.trackData

        embed {
            baseEmbed(event, "Now Playing:", image = event.guild.iconUrl)

            if (currentTrack.duration == Long.MAX_VALUE) {
                description(
                    "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                    "",
                    "$PLAY $LOADING Streaming $LOADING",
                    "",
                    "**Voice Channel**: ${musicPlayer.currentChannel!!.name}"
                )
            } else {
                description(
                    "**[${currentTrack.info.title}](${currentTrack.info.uri})** by **${currentTrack.info.author}**",
                    "",
                    "$PLAY ${progressBar(currentTrack.position, currentTrack.duration)} (`${musicLength(currentTrack.duration - currentTrack.position)}`)",
                    "",
                    "**Voice Channel**: ${musicPlayer.currentChannel!!.name}"
                )
            }

            thumbnail(musicManager.resolveThumbnail(currentTrack))

            val user = info.user
            if (user != null) {
                val member = event.guild.getMember(user)
                val requester = "**${member?.effectiveName ?: user.discordTag}**"

                field("Requested by:", requester, inline = true)
            }

            field("Duration:", musicLength(currentTrack.info.length, "Unknown"), inline = true)

            footer("Queue: ${queue.size} songs - ${musicLength(queue)} remaining | Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)
        }.send(event).queue()
    }

    override val helpHandler = HelpFactory("NowPlaying Command") {
        aliases("np")
        description("Displays the current track playing")

        seeAlso("play", "queue", "repeat")
    }
}