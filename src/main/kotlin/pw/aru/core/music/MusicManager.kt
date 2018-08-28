package pw.aru.core.music

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
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
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import okhttp3.OkHttpClient
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.utils.URIBuilder
import org.json.JSONObject
import pw.aru.core.listeners.EventListeners.submitTask
import pw.aru.utils.extensions.computeIfAbsent
import pw.aru.utils.extensions.newCall
import java.util.concurrent.atomic.AtomicInteger

class MusicManager(private val shardManager: ShardManager, private val httpClient: OkHttpClient, val eventWaiter: EventWaiter) {

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

    operator fun get(guild: Guild): GuildMusicPlayer = musicPlayers.computeIfAbsent(guild.idLong) { GuildMusicPlayer(shardManager, this, guild) }

    fun cleanup(guild: Guild) {
        val player = musicPlayers.remove(guild.idLong) ?: return
        submitTask("MusicManager cleanup") {
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
                    httpClient.newCall {
                        url(
                            URIBuilder("https://api.soundcloud.com/tracks/${currentTrack.identifier}")
                                .addParameter("client_id", sourceManager.clientId)
                                .build()
                                .toURL()
                        )
                    }.execute().use { response ->
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
