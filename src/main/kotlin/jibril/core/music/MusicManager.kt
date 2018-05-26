package jibril.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager.getChannelIdentifierFromUrl
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gnu.trove.map.TLongObjectMap
import gnu.trove.map.hash.TLongObjectHashMap
import jibril.Jibril
import jibril.core.listeners.EventListeners.submit
import jibril.utils.extensions.computeIfAbsent
import jibril.utils.extensions.newCall
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.utils.URIBuilder
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicManager @Inject constructor(private val shardManager: ShardManager) {

    companion object {
        val twitchTvThumbId = AtomicInteger()

        private fun userPlayerManager(): AudioPlayerManager {
            val m = DefaultAudioPlayerManager()

            m += YoutubeAudioSourceManager(true).apply {
                setPlaylistPageCount(20) //TODO Once we get premium, logic is "premium ? 128 : 4"

                configureRequests {
                    RequestConfig.copy(it).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()
                }
            }
            m += SoundCloudAudioSourceManager(true)
            m += BandcampAudioSourceManager()
            m += TwitchStreamAudioSourceManager()
            m += BeamAudioSourceManager()

            return m
        }

        private fun devPlayerManager(): AudioPlayerManager {
            val m = DefaultAudioPlayerManager()
            AudioSourceManagers.registerRemoteSources(m)
            AudioSourceManagers.registerLocalSource(m)
            return m
        }

        private fun httpSafePlayerManager(): AudioPlayerManager {
            val m = DefaultAudioPlayerManager()
            m += HttpAudioSourceManager()
            return m
        }
    }

    val musicPlayers: TLongObjectMap<GuildMusicPlayer> = TLongObjectHashMap()

    //User Input
    val userPlayerManager: AudioPlayerManager = userPlayerManager()

    //Attachments
    val httpSafePlayerManager: AudioPlayerManager = httpSafePlayerManager()

    //Developers
    val devPlayerManager: AudioPlayerManager = devPlayerManager()

    fun getMusicPlayer(guild: Guild): GuildMusicPlayer {
        return musicPlayers.computeIfAbsent(guild.idLong) { GuildMusicPlayer(shardManager, this, guild) }
    }

    fun cleanup(guild: Guild) {
        val player = musicPlayers.remove(guild.idLong) ?: return
        submit("MusicManager cleanup") {
            val audioPlayer = player.audioPlayer
            audioPlayer.removeListener(player)
            audioPlayer.destroy()
        }
    }

    fun resolveThumbnail(currentTrack: AudioTrack): String? {
        val trackData = currentTrack.trackData

        if (trackData.thumbnail != null) return trackData.thumbnail

        val sourceManager = currentTrack.sourceManager

        val thumbnail = when (sourceManager) {
            is YoutubeAudioSourceManager -> "https://img.youtube.com/vi/${currentTrack.identifier}/0.jpg"
            is TwitchStreamAudioSourceManager -> {
                return "https://static-cdn.jtvnw.net/previews-ttv/live_user_${getChannelIdentifierFromUrl(currentTrack.identifier)}-854x480.jpg&t=${twitchTvThumbId.incrementAndGet()}"
            }
            is SoundCloudAudioSourceManager -> {

                fun request(retry: Boolean = true): String? {
                    val response = Jibril.httpClient.newCall {
                        url(
                            URIBuilder("https://api.soundcloud.com/tracks/${currentTrack.identifier}")
                                .addParameter("client_id", sourceManager.clientId)
                                .build()
                                .toURL()
                        )
                    }.execute()

                    if (response.code() == 401) {
                        return if (retry) {
                            sourceManager.updateClientId()
                            request(false)
                        } else {
                            null
                        }
                    }

                    return try {
                        JSONObject(response.body()!!.string())
                            .getString("artwork_url")
                            .replace("large", "t500x500")
                    } catch (_: Exception) {
                        null
                    }
                }

                request()
            }
            else -> null
        }
        trackData.thumbnail = thumbnail

        return thumbnail
    }
}

private operator fun AudioPlayerManager.plusAssign(sourceManager: AudioSourceManager) {
    registerSourceManager(sourceManager)
}
