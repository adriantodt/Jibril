package pw.aru.bot.music.utils

import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.apache.http.client.utils.URIBuilder
import pw.aru.utils.extensions.lang.send
import pw.aru.utils.extensions.lib.toJsonObject
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.util.concurrent.atomic.AtomicInteger

object ThumbnailResolver {
    private val httpClient = HttpClient.newHttpClient()
    private val twitchTvThumbId = AtomicInteger()

    fun resolveThumbnail(track: AudioTrack): String? {
        val sourceManager = track.sourceManager

        when (sourceManager) {
            is YoutubeAudioSourceManager -> return "https://img.youtube.com/vi/${track.identifier}/0.jpg"
            is TwitchStreamAudioSourceManager -> return "https://static-cdn.jtvnw.net/previews-ttv/live_user_${
            TwitchStreamAudioSourceManager.getChannelIdentifierFromUrl(track.identifier)
            }-854x480.jpg&t=${twitchTvThumbId.incrementAndGet()}"
            is SoundCloudAudioSourceManager -> {
                fun request(retry: Boolean = true): String? {
                    val response = httpClient.send(ofString()) {
                        uri(
                            URIBuilder("https://api.soundcloud.com/tracks/${track.identifier.split('|').first()}")
                                .addParameter("client_id", sourceManager.clientId)
                                .build()
                        )
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
                        response
                            .body().toJsonObject()
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