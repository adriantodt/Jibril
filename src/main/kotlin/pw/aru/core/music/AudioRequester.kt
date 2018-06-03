package pw.aru.core.music

import com.jagrosh.jdautilities.menu.OrderedMenu
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import mu.KLogging
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel
import pw.aru.utils.AruColors
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.emotes.DISAPPOINTED
import pw.aru.utils.emotes.X
import pw.aru.utils.emotes.YOUTUBE
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class AudioRequester private constructor(
    private val textChannel: TextChannel, private val member: Member, private val musicPlayer: GuildMusicPlayer,
    private val searchTerm: String, private val showDialog: Boolean,
    private val addFirst: Boolean,
    private val playNow: Boolean
) : AudioLoadResultHandler {

    override fun trackLoaded(track: AudioTrack) {
        if (!connect(textChannel, member.voiceState.channel)) return

        val data = TrackData(textChannel, member.user)

        track.trackData = data

        if (addFirst) {
            musicPlayer.queue.offerFirst(track)
        } else {
            musicPlayer.queue.offer(track)
        }

        if (musicPlayer.audioPlayer.playingTrack == null || playNow) {
            musicPlayer.startNext(true)
        } else if (textChannel.canTalk()) {
            val info = track.info

            textChannel.sendMessage(
                "${member.asMention} has added `${info.title}` (${musicLength(info.length)}) to the queue! [#${if (addFirst) 0 else musicPlayer.queue.size}]"
            ).queue()
        }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (!connect(textChannel, member.voiceState.channel)) return

        if (playlist.isSearchResult) {
            if (!showDialog) {
                trackLoaded(playlist.tracks[0])
                return
            }

            with(OrderedMenu.Builder()) {
                setEventWaiter(musicPlayer.musicManager.eventWaiter)
                useNumbers()
                addUsers(member.user)
                setTimeout(30, TimeUnit.SECONDS)

                setColor(member.guild.selfMember.color ?: AruColors.primary)

                useCancelButton(true)
                setDescription(
                    "$YOUTUBE **React this message with the appropriate number of the song**\n(or with $X in case none of these are correct)"
                )

                val trackSelection = playlist.tracks.take(3)

                trackSelection.map { it.info }
                    .forEach { addChoice("**[${it.title}](${it.uri}) (${musicLength(it.length)})** \uD83C\uDFB5\n") }

                setCancel {
                    textChannel.sendMessage("$DISAPPOINTED Music choice canceled!").queue()

                    if (musicPlayer.currentTrack == null) {
                        musicPlayer.stop()
                    }
                }

                setSelection { msg, i ->
                    try {
                        val audioTrack = trackSelection[i - 1]

                        trackLoaded(audioTrack)

                    } catch (e: ArrayIndexOutOfBoundsException) {
                        msg.editMessage("$X You've chosen an invalid option, silly!")
                            .embed(null)
                            .override(true)
                            .queue()
                    }
                }

                build()
            }.display(textChannel)
            return
        } else if (playlist.selectedTrack != null) {
            trackLoaded(playlist.selectedTrack)
            return
        }

        val queueSize = musicPlayer.queue.size.toLong()
        val position = if (addFirst) "0-${playlist.tracks.size}" else "$queueSize-${queueSize + playlist.tracks.size}"

        if (addFirst) {
            playlist.tracks.reversed().forEach {
                it.userData = TrackData(textChannel, member.user)
                musicPlayer.queue.offerFirst(it)
            }
        } else {
            playlist.tracks.forEach {
                it.userData = TrackData(textChannel, member.user)
                musicPlayer.queue.offer(it)
            }
        }

        val length = playlist.tracks.map { it.info.length }.sum()
        if (musicPlayer.audioPlayer.playingTrack == null || playNow) {
            musicPlayer.startNext(true)
        } else if (textChannel.canTalk()) {
            textChannel.sendMessage(
                "${member.asMention} has added `${playlist.name}` (${musicLength(length)}) to the queue! [#$position]"
            ).queue()
        }
    }

    override fun noMatches() {
        if (searchTerm.startsWith("ytsearch:") || searchTerm.startsWith("scsearch:")) {
            textChannel.sendMessage("$CONFUSED Weird, I didn't find any songs! Did you spell everything correctly?").queue()
        }
    }

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity == FriendlyException.Severity.COMMON) {
            textChannel.sendMessage(
                "$DISAPPOINTED Aw, I can't play this song. I think it's due to copyright issues."
            ).queue()
        } else {
            textChannel.sendMessage(
                "Uh... I got an error while loading this song. Do you mind trying again, please?"
            ).queue()
        }
    }

    companion object : KLogging() {
        fun loadAndPlay(
            textChannel: TextChannel, member: Member, musicPlayer: GuildMusicPlayer,
            searchTerm: String, playerManager: AudioPlayerManager,
            showDialog: Boolean, addFirst: Boolean, playNow: Boolean
        ): Future<*> {
            return playerManager.loadItem(
                searchTerm,
                AudioRequester(textChannel, member, musicPlayer, searchTerm, showDialog, addFirst, playNow)
            )
        }
    }
}
