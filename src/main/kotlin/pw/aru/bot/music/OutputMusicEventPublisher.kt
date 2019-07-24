package pw.aru.bot.music

import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.json.JSONArray
import org.json.JSONObject
import pw.aru._obsolete.v1.io.AruIO
import pw.aru.bot.music.entities.MusicEventSource
import pw.aru.bot.music.events.*
import pw.aru.bot.music.internal.LavaplayerLoadResult
import pw.aru.bot.music.internal.OutputMusicEventAdapter
import pw.aru.bot.music.utils.ThumbnailResolver.resolveThumbnail
import pw.aru.libs.andeclient.util.AudioTrackUtil.fromTrack
import pw.aru.utils.extensions.lang.especializationName
import pw.aru.utils.extensions.lib.jsonOf
import pw.aru.utils.extensions.lib.listeners

class OutputMusicEventPublisher(val io: AruIO) : OutputMusicEventAdapter() {
    private fun publish(type: String, data: JSONObject) {
        io.sendFeed(type, data)
    }

    override fun onLoadResultsEvent(event: LoadResultsEvent) {
        if (event.source is MusicEventSource.Dashboard) {
            publish(
                "load_results",
                jsonOf(
                    "player" to serialize(event.player),
                    "source" to serialize(event.player.guild, event.source),
                    "results_id" to event.id.toString(),
                    "results" to serialize(event.results)
                )
            )
        }
    }

    override fun onTrackQueuedEvent(event: TrackQueuedEvent) {
        publish(
            "track_queued",
            jsonOf(
                "player" to serialize(event.player),
                "source" to serialize(event.player.guild, event.source),
                "track" to serialize(event.track)
            )
        )
    }

    override fun onPlaylistQueuedEvent(event: PlaylistQueuedEvent) {
        TODO("onPlaylistQueuedEvent")
    }

    override fun onConnectErrorEvent(event: ConnectErrorEvent) {
        TODO("onConnectErrorEvent")
    }

    override fun onMusicStartedEvent(event: MusicStartedEvent) {
        TODO("onMusicStartedEvent")
    }

    override fun onChangedVolumeEvent(event: ChangedVolumeEvent) {
        TODO("onChangedVolumeEvent")
    }

    override fun onChangedPauseStateEvent(event: ChangedPauseStateEvent) {
        TODO("onChangedPauseStateEvent")
    }

    override fun onChangedRepeatModeEvent(event: ChangedRepeatModeEvent) {
        TODO("onChangedRepeatModeEvent")
    }

    override fun onQueueShuffledEvent(event: QueueShuffledEvent) {
        TODO("onQueueShuffledEvent")
    }

    override fun onQueueClearedEvent(event: QueueClearedEvent) {
        TODO("onQueueClearedEvent")
    }

    override fun onTrackGotStuckEvent(event: TrackGotStuckEvent) {
        TODO("onTrackGotStuckEvent")
    }

    override fun onTrackErroredEvent(event: TrackErroredEvent) {
        TODO("onTrackErroredEvent")
    }

    override fun onTrackSkippedEvent(event: TrackSkippedEvent) {
        TODO("onTrackSkippedEvent")
    }

    override fun onNextTrackEvent(event: NextTrackEvent) {
        TODO("onNextTrackEvent")
    }

    override fun onMusicEndedEvent(event: MusicEndedEvent) {
        TODO("onMusicEndedEvent")
    }

    override fun onChangedVoteEvent(event: ChangedVoteEvent) {
        TODO("onChangedVoteEvent")
    }

    override fun onListenersLeftEvent(event: ListenersLeftEvent) {
        TODO("onListenersLeftEvent")
    }

    override fun onPlayerInfoEvent(event: PlayerInfoEvent) {
        TODO("onPlayerInfoEvent")
    }

    private fun serialize(guild: Guild): JSONObject {
        return jsonOf(
            "id" to guild.id(),
            "name" to guild.name(),
            "icon" to (guild.iconUrl() ?: "https://cdn.discordapp.com/embed/avatars/${guild.idAsLong() % 5}.png"),
            "region" to guild.region()
        )
    }

    private fun serialize(channel: VoiceChannel): JSONObject {
        return jsonOf(
            "id" to channel.id(),
            "name" to channel.name(),
            "listeners" to JSONArray(channel.listeners.map { serialize(it.member()!!) })
        )
    }

    private fun serialize(member: Member): JSONObject {
        val user = member.user()
        return jsonOf(
            "id" to member.id(),
            "username" to user.username(),
            "discriminator" to user.discriminator(),
            "discordTag" to user.discordTag(),
            "avatar" to user.effectiveAvatarUrl(),
            "nick" to member.nick(),
            "effectiveName" to member.effectiveName()
        )
    }

    private fun serialize(player: MusicPlayer): JSONObject {
        return jsonOf(
            "guild" to serialize(player.guild),
            "voiceChannel" to player.voiceChannel?.let(this::serialize)
        )
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
                return jsonOf(
                    "source" to "dashboard",
                    "id" to source.userId.toString(),
                    "member" to guild.members().getById(source.userId)?.let(this::serialize)
                )
            }
            is MusicEventSource.Discord -> {
                return jsonOf(
                    "source" to "discord",
                    "id" to source.user.id(),
                    "member" to source.member(guild)?.let(this::serialize)
                )
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

    private fun serialize(playlist: AudioPlaylist): JSONObject {
        val tracks = JSONArray(playlist.tracks.map(this::serialize))

        return jsonOf(
            "name" to playlist.name,
            "tracks" to tracks,
            "selected" to playlist.selectedTrack?.info?.identifier?.let {
                tracks.withIndex().firstOrNull { (_, value) -> (value as JSONObject)["identifier"] == it }?.index
            }
        )
    }
}