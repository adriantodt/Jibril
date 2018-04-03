package jibril.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.core.audio.AudioSendHandler

class AudioSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private lateinit var lastFrame: AudioFrame

    override fun canProvide(): Boolean {
        lastFrame = audioPlayer.provide() ?: return false
        return true
    }

    override fun provide20MsAudio(): ByteArray = lastFrame.data

    override fun isOpus() = true

}
