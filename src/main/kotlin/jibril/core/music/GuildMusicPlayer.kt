package jibril.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import jibril.utils.TaskManager.schedule
import jibril.utils.TaskType
import jibril.utils.emotes.*
import jibril.utils.extensions.*
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel
import java.util.concurrent.BlockingDeque
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class GuildMusicPlayer(private val shardManager: ShardManager, private val musicManager: MusicManager, guild: Guild) : AudioEventAdapter() {

    enum class RepeatMode {
        NONE, QUEUE, SONG;

        fun cycleNext(): RepeatMode {
            val values = values()
            val next = ordinal + 1
            return values[if (next == values.size) 0 else next]
        }
    }

    val audioPlayer: AudioPlayer = musicManager.userPlayerManager.createPlayer()
    var queue: BlockingDeque<AudioTrack> = LinkedBlockingDeque()
    val voteSkips: TLongList = TLongArrayList()
    val voteStops: TLongList = TLongArrayList()
    val voteShuffles: TLongList = TLongArrayList()
    val votePauses: TLongList = TLongArrayList()
    var repeatMode: RepeatMode = RepeatMode.NONE

    private var disconnectTask: Future<*>? = null
    private val guildId: Long = guild.idLong
    private var previousTrack: AudioTrack? = null

    val currentChannel: VoiceChannel?
        get() = guild.audioManager.connectedChannel

    val currentTrack: AudioTrack?
        get() = audioPlayer.playingTrack

    val guild: Guild
        get() = shardManager.getGuildById(guildId)

    init {
        this.audioPlayer.addListener(this)
        guild.audioManager.sendingHandler = AudioSendHandler(audioPlayer)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        player.isPaused = false
        voteSkips.clear()
        announce(track)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        previousTrack = track
        val info = track.trackData
        if (info.messageId > 0) {
            val channel = info.textChannel
            if (channel?.canTalk() == true) {
                channel.deleteMessageById(info.messageId).queue(null) { }
            }
        }

        if (endReason.mayStartNext) startNext(false)
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException?) {
        track.stop()
        val info = track.trackData
        val channel = info.textChannel
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$ERROR There was an error while playing `${track.info.title}`, skipping..."
            ).queue()
        }
        startNext(true)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        track.stop()
        val info = track.trackData
        val channel = info.textChannel
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$ERROR The song `${track.info.title}` got stuck while playing. Skipping..."
            ).queue()
        }

        startNext(true)
    }

    private fun announce(track: AudioTrack) {
        val info = track.trackData
        val channel = info.textChannel
        if (channel?.canTalk() == true) {
            embed {
                baseEmbed("Now Playing:", image = channel.guild.iconUrl)

                if (track.duration == Long.MAX_VALUE) {
                    description(
                        "**[${track.info.title}](${track.info.uri})** by **${track.info.author}**",
                        "",
                        "$PLAY $LOADING Streaming $LOADING",
                        "",
                        "**Voice Channel**: ${currentChannel!!.name}"
                    )
                } else {
                    description(
                        "**[${track.info.title}](${track.info.uri})** by **${track.info.author}**",
                        "",
                        "$PLAY ${progressBar(track.position, track.duration)} (`${musicLength(track.duration - track.position)}`)",
                        "",
                        "**Voice Channel**: ${currentChannel!!.name}"
                    )
                }
                thumbnail(musicManager.resolveThumbnail(track))

                val user = info.user
                if (user != null) {
                    val member = channel.guild.getMember(user)
                    val requester = "**${member?.effectiveName ?: user.discordTag}**"

                    field("Requested by:", requester, inline = true)
                }

                field("Duration:", musicLength(track.info.length, "Unknown"), inline = true)

                footer("Queue: ${queue.size} songs", channel.guild.iconUrl)

            }.send(channel).queue { info.messageId = it.idLong }
        }
    }

    fun cancelLeave() {
        if (disconnectTask == null) return
        disconnectTask!!.cancel(true)
        disconnectTask = null
        this.audioPlayer.isPaused = false
        val info = audioPlayer.playingTrack?.trackData ?: return
        val channel = info.textChannel
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$SMILEY *Yay someone joined me to listen to some nice songs!*\nI've resumed from where I stopped for you!"
            ).queue()
        }
    }

    fun scheduleLeave() {
        this.audioPlayer.isPaused = true

        disconnectTask = schedule(2, TimeUnit.MINUTES, TaskType.BUNK) {
            val info = audioPlayer.playingTrack?.trackData
            stop()
            val channel = info?.textChannel
            if (channel?.canTalk() == true) {
                channel.sendMessage(
                    "*Seems like no one is coming... D-did I do something wrong?* $CONFUSED\n I left the voice channel and stopped the queue."
                ).queue()
            }
        }
    }

    fun startNext(isSkipped: Boolean) {
        val track = previousTrack

        if (track != null) {
            if (repeatMode == RepeatMode.SONG && !isSkipped) {
                this.audioPlayer.playTrack(track.makeCloneWithData())
                return
            }

            if (repeatMode == RepeatMode.QUEUE) {
                queue.offer(track.makeCloneWithData())
            }
        }

        val next = queue.poll()

        if (next == null) {
            stop()
            return
        }

        audioPlayer.playTrack(next)
    }

    fun stop() {
        this.queue.clear()
        this.audioPlayer.stopTrack()
        this.audioPlayer.isPaused = false
        this.audioPlayer.volume = 100
        disconnect(guild.audioManager)
        musicManager.cleanup(guild)
    }
}

private fun AudioTrack.makeCloneWithData(): AudioTrack {
    val track = makeClone()
    track.userData = userData
    return track
}
