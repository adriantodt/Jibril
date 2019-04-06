package pw.aru.core.music

import com.github.samophis.lavaclient.util.AudioTrackUtil.fromTrack
import com.mewna.catnip.entity.guild.Guild
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.json.JSONArray
import org.json.JSONObject
import pw.aru.core.music.entities.MusicEventSource
import pw.aru.core.music.events.*
import pw.aru.core.music.internal.LavaplayerLoadResult
import pw.aru.core.music.internal.OutputMusicEventAdapter
import pw.aru.core.music.utils.ThumbnailResolver.resolveThumbnail
import pw.aru.io.AruIO
import pw.aru.utils.extensions.lang.especializationName
import pw.aru.utils.extensions.lib.jsonOf

class OutputMusicEventPublisher(val io: AruIO) : OutputMusicEventAdapter() {
    private fun publish(type: String, data: JSONObject) {
        io.sendFeed(type, data)
    }

    override fun onLoadResultsEvent(event: LoadResultsEvent) {
        if (event.source is MusicEventSource.Dashboard) {
            publish(
                "load-results",
                jsonOf(
                    "id" to event.id.toString(),
                    "results" to serialize(event.results)
                )
            )
        }
    }

    override fun onTrackQueuedEvent(event: TrackQueuedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlaylistQueuedEvent(event: PlaylistQueuedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectErrorEvent(event: ConnectErrorEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMusicStartedEvent(event: MusicStartedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChangedVolumeEvent(event: ChangedVolumeEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChangedPauseStateEvent(event: ChangedPauseStateEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChangedRepeatModeEvent(event: ChangedRepeatModeEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueueShuffledEvent(event: QueueShuffledEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueueClearedEvent(event: QueueClearedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTrackGotStuckEvent(event: TrackGotStuckEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTrackErroredEvent(event: TrackErroredEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTrackSkippedEvent(event: TrackSkippedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNextTrackEvent(event: NextTrackEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMusicEndedEvent(event: MusicEndedEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChangedVoteEvent(event: ChangedVoteEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onListenersLeftEvent(event: ListenersLeftEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerInfoEvent(event: PlayerInfoEvent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun serialize(guild: Guild, source: MusicEventSource): JSONObject {
        when (source) {
            MusicEventSource.AndesiteNode -> {
                return jsonOf("authority" to "lavalink")
            }
            MusicEventSource.MusicSystem -> {
                return jsonOf("authority" to "music_system")
            }
            MusicEventSource.VotingSystem -> {
                return jsonOf("authority" to "voting_system")
            }
            is MusicEventSource.Dashboard -> {
                val j = JSONObject()
                j.put("source", "dashboard")

                guild.members().getById(source.userId)?.let { member ->
                    val user = member.user()
                    j.put(
                        "data",
                        jsonOf(
                            "username" to user.username(),
                            "discriminator" to user.discriminator(),
                            "discordTag" to user.discordTag(),
                            "avatar" to user.effectiveAvatarUrl(),
                            "nick" to member.nick(),
                            "effectiveName" to member.effectiveName()
                        )
                    )
                }

                return j
            }
            is MusicEventSource.Discord -> {
                val j = JSONObject()
                j.put("source", "discord")

                source.member(guild)?.let { member ->
                    val user = member.user()
                    j.put(
                        "data",
                        jsonOf(
                            "username" to user.username(),
                            "discriminator" to user.discriminator(),
                            "discordTag" to user.discordTag(),
                            "avatar" to user.effectiveAvatarUrl(),
                            "nick" to member.nick(),
                            "effectiveName" to member.effectiveName()
                        )
                    )
                }

                return j
            }
        }
    }

    private fun serialize(results: LavaplayerLoadResult): JSONObject {
        return when (results) {
            is LavaplayerLoadResult.NoMatches -> jsonOf(
                "id" to results.id,
                "type" to "empty"
            )
            is LavaplayerLoadResult.Failed -> jsonOf(
                "id" to results.id,
                "type" to "failed",
                "err" to jsonOf(
                    "type" to results.exception.especializationName(),
                    "message" to results.exception.message,
                    "severity" to results.exception.severity.name.toLowerCase()
                )
            )
            is LavaplayerLoadResult.SearchResults -> jsonOf(
                "id" to results.id,
                "type" to "search",
                "results" to serialize(results.results)
            )
            is LavaplayerLoadResult.Playlist -> jsonOf(
                "id" to results.id,
                "type" to "playlist",
                "results" to serialize(results.playlist)
            )
            is LavaplayerLoadResult.Track -> jsonOf(
                "id" to results.id,
                "type" to "track",
                "result" to serialize(results.track)
            )
        }
    }

    private fun serialize(results: AudioPlaylist): JSONObject {
        val j = JSONObject()

        val ja = JSONArray()

        for (track in results.tracks) {
            ja.put(serialize(track))
        }

        j.put("tracks", ja)

        results.selectedTrack?.info?.identifier.let {
            j.put("selected", ja.withIndex().find { (it.value as JSONObject).get("identifier") == it }!!.index)
        }

        return j
    }

    private fun serialize(track: AudioTrack): JSONObject {
        val info = track.info

        return jsonOf(
            "data" to fromTrack(track),
            "title" to info.title,
            "author" to info.author,
            "length" to info.length,
            "identifier" to info.identifier,
            "isStream" to info.isStream,
            "uri" to info.uri,
            "thumbnail" to resolveThumbnail(track)
        )
    }

}