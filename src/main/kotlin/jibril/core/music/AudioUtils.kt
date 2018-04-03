package jibril.core.music

import br.com.brjdevs.java.utils.async.Async
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.listeners.EventListeners.submit
import jibril.utils.DiscordUtils.stripFormatting
import jibril.utils.TaskManager.queue
import jibril.utils.TaskType
import jibril.utils.emotes.SUCCESS
import jibril.utils.emotes.X
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.managers.AudioManager
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

fun connect(channel: TextChannel?, vc: VoiceChannel): Boolean {

    val audioManager = vc.guild.audioManager

    if (vc == audioManager.connectedChannel) return true

    if (audioManager.isConnected && vc != audioManager.connectedChannel) {
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X Wait, I'm already connected to **${stripFormatting(audioManager.connectedChannel.name)}**, silly! Join it if you wanna hear some nice music!"
            ).queue()
        }

        return false
    }

    val selfMember = vc.guild.selfMember

    if (!selfMember.hasPermission(vc, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X Hey, I need permission to **Connect** and **Speak** in that voice channel so I can play music!"
            ).queue()
        }

        return false
    } else if (vc.userLimit > 0 && vc.userLimit <= vc.members.size) {
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X Hey, I can't join! The voice channel you're at is full!"
            ).queue()
        }

        return false
    }

    val task = queue(TaskType.BUNK) {
        audioManager.openAudioConnection(vc)
        while (!audioManager.isConnected) Async.sleep(100)
    }

    try {
        task[45, TimeUnit.SECONDS]
    } catch (e: Exception) {
        task.cancel(true)
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X I couldn't connect to the voice channel. Mind reporting this to my developer? (Check out `j!hangout`)"
            ).queue()
        }

        return false
    }

    if (channel?.canTalk() == true) {
        channel.sendMessage(
            "$SUCCESS Yay! Connected to voice channel **${stripFormatting(vc.name)}**!"
        ).queue()
    }

    return true
}

fun disconnect(audioManager: AudioManager): Future<*> {
    return submit("Audio Disconnect") {
        audioManager.closeAudioConnection()
    }
}

fun musicLength(millis: Long, stream: String = "stream"): String {
    if (millis == Long.MAX_VALUE) return stream

    val hours = millis / 3600000
    val minutes = millis / 60000 % 60
    val seconds = millis / 1000 % 60

    return if (hours == 0L) "%02d:%02d".format(minutes, seconds) else "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun musicLength(queue: Iterable<AudioTrack>): String {
    val length = queue.map { it.duration }.filter { it != Long.MAX_VALUE }.sum()
    val streamCount = queue.count { it.duration == Long.MAX_VALUE }

    if (length == 0L && streamCount != 0) {
        return "$streamCount streams"
    }

    if (length != 0L && streamCount == 0) {
        return musicLength(length)
    }

    return "${musicLength(length)} + ${if (streamCount == 1) "1 stream" else "$streamCount streams"}"
}

private const val BLOCK_INACTIVE = "\u25AC"
private const val BLOCK_ACTIVE = "\uD83D\uDD18"
private const val TOTAL_BLOCKS = 10

fun progressBar(position: Long, duration: Long): String {
    val active = (position.toDouble() / duration.toDouble()).times(TOTAL_BLOCKS).toInt()
    return (0..TOTAL_BLOCKS).joinToString("") { if (it == active) BLOCK_ACTIVE else BLOCK_INACTIVE }
}

var AudioTrack.trackData: TrackData
    get() = userData as TrackData
    set(value) {
        userData = value
    }
