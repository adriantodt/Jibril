package pw.aru.core.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.managers.AudioManager
import pw.aru.core.listeners.EventListeners.submitTask
import pw.aru.utils.TaskManager.queue
import pw.aru.utils.TaskType
import pw.aru.utils.emotes.SOUNDCLOUD
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.X
import pw.aru.utils.emotes.YOUTUBE
import pw.aru.utils.extensions.stripFormatting
import java.lang.Thread.sleep
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

enum class SearchType(val startsWith: String, val emote: String) {
    YouTube("ytsearch:", YOUTUBE), SoundCloud("scsearch:", SOUNDCLOUD), OTHER("", "");

    companion object {
        operator fun invoke(searchTerm: String) = values().asIterable().first { searchTerm.startsWith(it.startsWith) }
    }
}

fun connect(channel: TextChannel?, vc: VoiceChannel): Boolean {

    val audioManager = vc.guild.audioManager

    if (vc == audioManager.connectedChannel) return true

    if (audioManager.isConnected && vc != audioManager.connectedChannel) {
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X Wait, I'm already connected to **${audioManager.connectedChannel.name.stripFormatting()}**, silly! Join it if you wanna hear some nice music!"
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
        while (!audioManager.isConnected) sleep(100)
    }

    try {
        task[45, TimeUnit.SECONDS]
    } catch (e: Exception) {
        task.cancel(true)
        if (channel?.canTalk() == true) {
            channel.sendMessage(
                "$X I couldn't connect to the voice channel. Mind reporting this to my developer? (Check out `a!hangout`)"
            ).queue()
        }

        return false
    }

    if (channel?.canTalk() == true) {
        channel.sendMessage(
            "$SUCCESS Yay! Connected to voice channel **${vc.name.stripFormatting()}**!"
        ).queue()
    }

    return true
}

fun disconnect(audioManager: AudioManager): Future<*> {
    return submitTask("Audio Disconnect") {
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
    val length = queue.asSequence().filterNot { it.info.isStream }.map { it.duration }.sum()
    val streamCount = queue.count { it.info.isStream }

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
private const val TOTAL_BLOCKS = 15

fun progressBar(position: Long, duration: Long): String {
    val active = (position.toDouble() / duration.toDouble()).times(TOTAL_BLOCKS).toInt()
    return (0..TOTAL_BLOCKS).joinToString("") { if (it == active) BLOCK_ACTIVE else BLOCK_INACTIVE }
}

var AudioTrack.trackData: TrackData
    get() = userData as TrackData
    set(value) {
        userData = value
    }
