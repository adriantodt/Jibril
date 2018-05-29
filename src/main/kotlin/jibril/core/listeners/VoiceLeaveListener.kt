package jibril.core.listeners

import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.core.music.trackData
import jibril.utils.extensions.humanUsers
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.hooks.EventListener

class VoiceLeaveListener(private val musicManager: MusicManager) : EventListener {

    override fun onEvent(event: Event) {
        when (event) {
            is GuildVoiceLeaveEvent -> onVoiceLeave(event)
            is GuildVoiceJoinEvent -> onVoiceJoin(event)
            is GuildLeaveEvent -> onGuildLeave(event)
            is VoiceChannelDeleteEvent -> onChannelDelete(event)
        }
    }

    private fun onChannelDelete(event: VoiceChannelDeleteEvent) {
        if (!event.channel.members.contains(event.guild.selfMember)) return
        musicManager.cleanup(event.guild)
    }

    private fun onVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member == event.guild.selfMember) return
        if (event.member.user.isBot) return
        if (!event.channelLeft.members.contains(event.guild.selfMember)) return

        val musicPlayer = musicManager.getMusicPlayer(event.guild)

        arrayOf(
            musicPlayer.votePauses,
            musicPlayer.voteShuffles,
            musicPlayer.voteSkips,
            musicPlayer.voteStops
        ).forEach { it.remove(event.member.user.idLong) }

        val voiceChannel = musicPlayer.currentChannel

        if (voiceChannel == event.channelLeft && event.channelLeft.humanUsers == 0) {
            onLeftAlone(musicPlayer)
        }
    }

    private fun onVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.member == event.guild.selfMember) return

        if (!event.channelJoined.members.contains(event.guild.selfMember)) return

        val musicPlayer = musicManager.getMusicPlayer(event.guild)
        val voiceChannel = musicPlayer.currentChannel

        if (voiceChannel == event.channelJoined && event.channelJoined.humanUsers > 0) {
            musicPlayer.cancelLeave()
        }
    }

    private fun onGuildLeave(event: GuildLeaveEvent) {
        musicManager.cleanup(event.guild)
    }

    private fun onLeftAlone(musicPlayer: GuildMusicPlayer) {
        musicPlayer.scheduleLeave()
        val channel = musicPlayer.audioPlayer.playingTrack?.trackData?.textChannel

        if (channel != null && channel.canTalk()) {
            channel.sendMessage("I was left alone in the voice channel... If no one joins within **2 minutes**, I'm going to **leave the channel and stop the queue.**").queue()
        }
    }
}
