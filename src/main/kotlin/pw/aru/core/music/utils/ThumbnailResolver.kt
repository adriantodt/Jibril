package pw.aru.core.music.utils

import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import okhttp3.OkHttpClient
import org.apache.http.client.utils.URIBuilder
import org.json.JSONObject
import pw.aru.utils.extensions.lib.newCall
import java.util.concurrent.atomic.AtomicInteger

object ThumbnailResolver {
    private val httpClient = OkHttpClient()
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
                    httpClient.newCall {
                        url(
                            URIBuilder(
                                "https://api.soundcloud.com/tracks/${track.identifier.split(
                                    '|'
                                ).first()}"
                            )
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

                return request()
            }
            else -> return null
        }
    }
}