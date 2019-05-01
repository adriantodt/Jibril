package pw.aru.core.music.utils

import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.apache.http.client.utils.URIBuilder
import org.json.JSONObject
import pw.aru.exported.user_agent
import pw.aru.utils.extensions.lang.send
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.atomic.AtomicInteger

object ThumbnailResolver {
    private val httpClient = HttpClient.newHttpClient()
    private val twitchTvThumbId = AtomicInteger()

    fun resolveThumbnail(track: AudioTrack): String? {
        when (val sourceManager = track.sourceManager) {
            is YoutubeAudioSourceManager -> return "https://img.youtube.com/vi/${track.identifier}/0.jpg"
            is TwitchStreamAudioSourceManager -> return "https://static-cdn.jtvnw.net/previews-ttv/live_user_${
            TwitchStreamAudioSourceManager.getChannelIdentifierFromUrl(track.identifier)
            }-854x480.jpg&t=${twitchTvThumbId.incrementAndGet()}"
            is SoundCloudAudioSourceManager -> {
                fun request(retry: Boolean = true): String? {
                    val response = httpClient.send(BodyHandlers.ofString()) {
                        uri(
                            URIBuilder("https://api.soundcloud.com/tracks/${track.identifier.split('|').first()}")
                                .addParameter("client_id", sourceManager.clientId)
                                .build()
                        )
                        header("User-Agent", user_agent)
                    }

                    if (response.statusCode() == 401) {
                        return if (retry) {
                            sourceManager.updateClientId()
                            request(false)
                        } else {
                            null
                        }
                    }

                    return try {
                        JSONObject(response.body())
                            .getString("artwork_url")
                            .replace("large", "t500x500")
                    } catch (_: Exception) {
                        null
                    }
                }

                return request()
            }
            else -> return null
        }
    }
}