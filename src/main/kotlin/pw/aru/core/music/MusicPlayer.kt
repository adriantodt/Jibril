package pw.aru.core.music

import com.github.samophis.lavaclient.events.*
import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import com.mewna.catnip.entity.voice.VoiceServerUpdate
import com.mewna.catnip.shard.DiscordEvent
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import gg.amy.catnip.utilities.waiter.EventExtension
import gnu.trove.list.TLongList
import pw.aru.core.music.entities.*
import pw.aru.core.music.events.*
import pw.aru.core.music.internal.AbstractMusicPlayer
import pw.aru.core.music.internal.LavaplayerLoadHandler
import pw.aru.core.music.internal.LavaplayerLoadResult
import pw.aru.core.music.internal.TrackData
import pw.aru.core.music.utils.NowPlayingEmbed.nowPlayingEmbed
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.extensions.lang.getValue
import pw.aru.utils.extensions.lang.roundRobinFlatten
import pw.aru.utils.extensions.lib.humanUsers
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

class MusicPlayer(
    val musicSystem: MusicSystem, guild: Guild
) : AbstractMusicPlayer(musicSystem, guild.catnip(), guild.idAsLong()) {

    private val guildId = guild.idAsLong()
    val catnip = guild.catnip()!!
    private var lastTextChannelId: Long = 0
    private var lastMessage: Message? = null

    val lavaPlayer = musicSystem.lavaClient.newPlayer(guildId)
    var queue = LinkedBlockingDeque<MusicTrack>()
    var repeatMode = RepeatMode.NONE
    val voteMap = EnumMap<VoteType, TLongList>(VoteType::class.java)

    var lastPosition: Long = -1
    var lastTimestamp: Long = -1
    var lastTrackData: TrackData? = null

    var destroyed: Boolean = false

    val guild: Guild
        get() = catnip.cache().guild(guildId)!!

    val voiceChannel: VoiceChannel?
        get() = catnip.cache().voiceState(guildId, catnip.selfUser()!!.idAsLong())?.channel()

    val currentTrack: AudioTrack?
        get() = lavaPlayer.playingTrack()

    val textChannel: TextChannel?
        get() = catnip.cache().channel(guildId, lastTextChannelId)?.asTextChannel()

    init {
        lavaPlayer.connect(musicSystem.lavaClient.bestNode())
    }

    fun sendOrUpdateNowPlaying(memberRequested: Member? = null) {
        val m = lastMessage
        if (m != null) {
            m.edit(nowPlayingEmbed(this, memberRequested)).thenAccept { lastMessage = it }
        } else {
            val sentMessage by textChannel?.sendMessage(nowPlayingEmbed(this, memberRequested))
            sentMessage?.let { lastMessage = it }
        }
    }

    fun sendNowPlaying(memberRequested: Member? = null) {
        val m = lastMessage

        if (m != null) {
            try {
                m.delete()
            } catch (_: Exception) {
            }
        }

        sendOrUpdateNowPlaying(memberRequested)
    }

    override fun onLoadItemEvent(event: LoadItemEvent) {
        eagerHandle(event)

        try {
            when (URL(event.itemIdentifier).host) {
                "cdn.discordapp.com", "media.discordapp.com" -> loadItem(event, ItemSource.HTTP_SAFE)
                else -> loadItem(event, event.itemSourceType)
            }
        } catch (e: Exception) {
            searchItems(event)
        }
    }

    override fun onEnqueueTrackEvent(event: EnqueueTrackEvent) {
        if (!requireConnected(event.source)) return

        val musicTrack = MusicTrack(event.track, TrackData(event.source, event.track, event.trackLoadOptions))

        when (event.trackLoadOptions.enqueueLoadMode) {
            EnqueueLoadMode.NOW -> {
                queue.offerFirst(musicTrack)
                publish(TrackQueuedEvent(this, event.source, event.track))
                publish(SkipTrackEvent(event.source))
                return
            }
            EnqueueLoadMode.DEFAULT -> {
                queue.offerLast(musicTrack)
                publish(TrackQueuedEvent(this, event.source, event.track))
            }
            EnqueueLoadMode.NEXT -> {
                queue.offerFirst(musicTrack)
                publish(TrackQueuedEvent(this, event.source, event.track))
            }
        }

        if (lavaPlayer.playingTrack() == null) {
            publish(MusicStartedEvent(this, event.source))
            startNext()
        }
    }

    override fun onEnqueuePlaylistEvent(event: EnqueuePlaylistEvent) {
        if (!requireConnected(event.source)) return

        val tracks = event.playlist.tracks
            .map { MusicTrack(it, TrackData(event.source, it, event.trackLoadOptions)) }
            .let { if (event.trackLoadOptions.shufflePlaylist) it.shuffled() else it }

        when (event.trackLoadOptions.enqueueLoadMode) {
            EnqueueLoadMode.NOW -> {
                tracks.reversed().forEach { queue.offerFirst(it) }
                publish(PlaylistQueuedEvent(this, event.source, event.playlist))
                publish(SkipTrackEvent(event.source))
                return
            }
            EnqueueLoadMode.DEFAULT -> {
                tracks.forEach { queue.offerLast(it) }
                publish(PlaylistQueuedEvent(this, event.source, event.playlist))
            }
            EnqueueLoadMode.NEXT -> {
                tracks.reversed().forEach { queue.offerFirst(it) }
                publish(PlaylistQueuedEvent(this, event.source, event.playlist))
            }
        }

        if (lavaPlayer.playingTrack() == null) {
            publish(MusicStartedEvent(this, event.source))
            startNext()
        }
    }

    override fun onChangeVolumeEvent(event: ChangeVolumeEvent) {
        eagerHandle(event)

        lavaPlayer.volume(event.volume)
        publish(ChangedVolumeEvent(this, event.source, event.volume))
    }

    override fun onChangePauseStateEvent(event: ChangePauseStateEvent) {
        eagerHandle(event)

        when (event.state) {
            null -> {
                if (lavaPlayer.paused()) {
                    lavaPlayer.resume()
                    publish(ChangedPauseStateEvent(this, event.source, PauseState.RESUMED))
                } else {
                    lavaPlayer.pause()
                    publish(ChangedPauseStateEvent(this, event.source, PauseState.PAUSED))
                }
            }
            PauseState.PAUSED -> {
                if (!lavaPlayer.paused()) lavaPlayer.pause()
                publish(ChangedPauseStateEvent(this, event.source, event.state))
            }
            PauseState.RESUMED -> {
                if (lavaPlayer.paused()) lavaPlayer.resume()
                publish(ChangedPauseStateEvent(this, event.source, event.state))
            }
        }
    }

    override fun onChangeRepeatModeEvent(event: ChangeRepeatModeEvent) {
        eagerHandle(event)

        if (event.mode == null) {
            val mode = repeatMode.cycleNext()
            repeatMode = mode
            publish(ChangedRepeatModeEvent(this, event.source, mode))
        } else {
            repeatMode = event.mode
            publish(ChangedRepeatModeEvent(this, event.source, event.mode))
        }
    }

    override fun onChangeMusicPositionEvent(event: ChangeMusicPositionEvent) {
        eagerHandle(event)

        lavaPlayer.seek(event.position)
    }

    override fun onShuffleQueueEvent(event: ShuffleQueueEvent) {
        eagerHandle(event)

        queue = LinkedBlockingDeque(queue.shuffled())
        publish(QueueShuffledEvent(this, event.source))
    }

    override fun onClearQueueEvent(event: InputMusicEvent) {
        eagerHandle(event)

        queue.clear()
        publish(QueueClearedEvent(this, event.source))
    }

    override fun onRemoveTrackEvent(event: RemoveTrackEvent) {
        eagerHandle(event)

        queue = queue.asSequence().withIndex()
            .filter { (i) -> i !in event.range }
            .map { it.value }
            .toCollection(LinkedBlockingDeque())
    }

    override fun onSkipTrackEvent(event: SkipTrackEvent) {
        eagerHandle(event)

        publish(TrackSkippedEvent(this, event.source))
        startNext(currentTrack, true)
    }

    override fun onStopMusicEvent(event: StopMusicEvent) {
        eagerHandle(event)

        when (event.source) {
            MusicEventSource.MusicSystem -> {
                stop(if (event.silent) MusicStopReason.SilentQuit else MusicStopReason.ChannelDeleted)
            }
            is MusicEventSource.Dashboard, is MusicEventSource.Discord -> {
                stop(MusicStopReason.UserCommand(event.source))
            }
            else -> throw IllegalStateException("wtf event source is ${event.source}")
        }
    }

    override fun onToggleVoteEvent(event: ToggleVoteEvent) {
        eagerHandle(event)

        val votes = voteMap[event.type]!!

        val id = event.source.member(guild)?.idAsLong()
            ?: throw IllegalStateException("wtf event source is ${event.source}")

        val toAdd = !votes.contains(id)

        if (toAdd) {
            votes.add(id)
        } else {
            votes.remove(id)
        }

        val requiredVotes = (voiceChannel!!.humanUsers * 0.6).toInt()
        val voteCount = votes.size()
        val votesReached = requiredVotes >= voteCount
        publish(ChangedVoteEvent(this, event.source, event.type, toAdd, requiredVotes - voteCount))

        if (!votesReached) return
        votes.clear()

        publish(
            when (event.type) {
                VoteType.SKIP -> SkipTrackEvent(MusicEventSource.VotingSystem)
                VoteType.STOP -> StopMusicEvent(MusicEventSource.VotingSystem)
                VoteType.SHUFFLE -> ShuffleQueueEvent(MusicEventSource.VotingSystem)
                VoteType.PAUSE -> ChangePauseStateEvent(MusicEventSource.VotingSystem, PauseState.PAUSED)
                VoteType.RESUME -> ChangePauseStateEvent(MusicEventSource.VotingSystem, PauseState.RESUMED)
                VoteType.CLEAR_QUEUE -> ClearQueueEvent(MusicEventSource.VotingSystem)
            }
        )
    }

    override fun onDiscordListenersLeftEvent(event: DiscordListenersLeftEvent) {
        publish(ListenersLeftEvent(this, ListenersLeftState.LEFT_ALONE))

        var channelId = voiceChannel!!.idAsLong()
        val selfId = catnip.selfUser()!!.idAsLong()

        catnip.extensionManager().extension(EventExtension::class.java)!!
            .waitForEvent(DiscordEvent.VOICE_STATE_UPDATE)
            .condition {
                when {
                    //someone joined
                    it.channelIdAsLong() == channelId && it.userIdAsLong() != selfId -> {
                        true
                    }
                    //bot moved to another channel (or channel deleted)
                    it.userIdAsLong() == selfId && it.channelIdAsLong() != channelId -> {
                        val channel = it.channel()

                        if (channel == null) {
                            true
                        } else {
                            channelId = channel.idAsLong()
                            channel.humanUsers > 0
                        }
                    }
                    else -> false
                }
            }
            .timeout(2, TimeUnit.MINUTES) {
                if (destroyed) return@timeout

                stop(MusicStopReason.LeftAlone)
            }
            .action {
                if (destroyed) return@action

                if (it.channel() != null) {
                    publish(ListenersLeftEvent(this, ListenersLeftState.RETURNED))
                }
            }
    }

    override fun onTrackStartEvent(event: TrackStartEvent) {
        lavaPlayer.resume()
        publish(NextTrackEvent(this, event.track()))
    }

    override fun onPlayerUpdateEvent(event: PlayerUpdateEvent) {
        lastPosition = event.position()
        lastTimestamp = event.timestamp()
        publish(
            PlayerInfoEvent(
                this,
                lastTimestamp,
                lastPosition,
                MusicTrack(currentTrack!!, lastTrackData!!),
                queue.toList()
            )
        )
    }

    override fun onTrackStuckEvent(event: TrackStuckEvent) {
        publish(TrackGotStuckEvent(this, event.track()))
        startNext()
    }

    override fun onTrackExceptionEvent(event: TrackExceptionEvent) {
        publish(TrackErroredEvent(this, event.track(), event.exception().reason()))
        startNext()
    }

    override fun onTrackEndEvent(event: TrackEndEvent) {
        if (event.reason().mayStartNext) startNext(event.track())
    }

    private fun eagerHandle(event: InputMusicEvent) {
        if (lastTextChannelId == 0L) {
            event.source.channel(guild)?.let { lastTextChannelId = it.idAsLong() }
        }
    }

    private fun searchItems(event: LoadItemEvent) {
        val manager = musicSystem.sources.getValue(event.itemSourceType)

        val futures = listOf(
            CompletableFuture<AudioPlaylist>().apply {
                manager.loadItem("ytsearch: ${event.itemIdentifier.trim()}",
                    LavaplayerLoadHandler(event.id) {
                        when (it) {
                            is LavaplayerLoadResult.SearchResults -> complete(it.results)
                            else -> completeExceptionally(Throwable())
                        }
                    }
                )
            },
            CompletableFuture<AudioPlaylist>().apply {
                manager.loadItem("scsearch: ${event.itemIdentifier.trim()}",
                    LavaplayerLoadHandler(event.id) {
                        when (it) {
                            is LavaplayerLoadResult.SearchResults -> complete(it.results)
                            else -> completeExceptionally(Throwable())
                        }
                    }
                )
            }
        )

        queue {
            val results = futures.mapNotNull { it.runCatching { join() }.getOrNull() }
            handleResults(
                event,
                LavaplayerLoadResult.SearchResults(
                    event.id,
                    BasicAudioPlaylist(
                        results.first().name,
                        results.map { it.tracks }.roundRobinFlatten(),
                        null,
                        true
                    )
                )
            )
        }
    }

    private fun loadItem(event: LoadItemEvent, itemSourceType: ItemSource) {
        musicSystem.sources.getValue(itemSourceType).loadItem(
            event.itemIdentifier, LavaplayerLoadHandler(event.id) {
                handleResults(event, it)
            }
        )
    }

    private fun handleResults(event: LoadItemEvent, result: LavaplayerLoadResult) {
        when (result) {
            is LavaplayerLoadResult.NoMatches, is LavaplayerLoadResult.Failed -> {
                publish(LoadResultsEvent(this, event.source, event.id, result, event.trackLoadOptions))
            }
            is LavaplayerLoadResult.SearchResults -> {
                when (event.loadItemMode) {
                    LoadItemMode.DEFAULT, LoadItemMode.CHOOSE -> {
                        publish(LoadResultsEvent(this, event.source, event.id, result, event.trackLoadOptions))
                    }
                    LoadItemMode.FORCE -> {
                        publish(EnqueueTrackEvent(event.source, result.results.tracks[0], event.trackLoadOptions))
                    }
                }
            }
            is LavaplayerLoadResult.Playlist -> {
                when (event.loadItemMode) {
                    LoadItemMode.DEFAULT -> {
                        publish(EnqueuePlaylistEvent(event.source, result.playlist, event.trackLoadOptions))
                    }
                    LoadItemMode.CHOOSE -> {
                        publish(LoadResultsEvent(this, event.source, event.id, result, event.trackLoadOptions))
                    }
                    LoadItemMode.FORCE -> {
                        val selected = result.playlist.selectedTrack
                        if (selected != null) {
                            publish(EnqueueTrackEvent(event.source, selected, event.trackLoadOptions))
                        } else {
                            publish(EnqueuePlaylistEvent(event.source, result.playlist, event.trackLoadOptions))
                        }
                    }
                }
            }
            is LavaplayerLoadResult.Track -> {
                when (event.loadItemMode) {
                    LoadItemMode.DEFAULT, LoadItemMode.FORCE -> {
                        publish(EnqueueTrackEvent(event.source, result.track, event.trackLoadOptions))
                    }
                    LoadItemMode.CHOOSE -> {
                        publish(LoadResultsEvent(this, event.source, event.id, result, event.trackLoadOptions))
                    }
                }
            }
        }
    }

    private fun requireConnected(source: MusicEventSource): Boolean {
        if (source is MusicEventSource.AndesiteNode) throw IllegalStateException("wtf source is Andesite")
        if (source is MusicEventSource.MusicSystem) throw IllegalStateException("wtf source is MusicSystem")

        val member = source.member(guild) ?: throw IllegalStateException("wtf source is $source")

        val memberVoiceChannel = catnip.cache().voiceState(guildId, member.idAsLong())?.channel()
        val botVoiceChannel = voiceChannel

        if (memberVoiceChannel == null) {
            publish(ConnectErrorEvent(this, source, ConnectionErrorType.MEMBER_NOT_CONNECTED))
            return false
        }

        if (botVoiceChannel != null && memberVoiceChannel != botVoiceChannel) {
            publish(ConnectErrorEvent(this, source, ConnectionErrorType.BOT_CONNECTED_TO_OTHER_CHANNEL))
            return false
        }
        val selfMember = guild.selfMember()

        if (!selfMember.hasPermissions(memberVoiceChannel, Permission.CONNECT, Permission.SPEAK)) {
            publish(ConnectErrorEvent(this, source, ConnectionErrorType.BOT_CANT_CONNECT))
            return false
        }

        if (memberVoiceChannel.userLimit() != 0) {
            val channelFull = memberVoiceChannel.guild().voiceStates()
                .count { it.channelIdAsLong() == memberVoiceChannel.idAsLong() } >= memberVoiceChannel.userLimit()
            val vcManager = selfMember.hasPermissions(memberVoiceChannel, Permission.MANAGE_CHANNELS)

            if (channelFull && !vcManager) {
                publish(ConnectErrorEvent(this, source, ConnectionErrorType.MEMBER_CHANNEL_FULL))
                return false
            }
        }

        val connected = CompletableFuture<VoiceServerUpdate>()

        catnip.extensionManager().extension(EventExtension::class.java)!!.waitForEvent(DiscordEvent.VOICE_SERVER_UPDATE)
            .condition { it.guildIdAsLong() == guildId }
            .action { connected.complete(it) }

        catnip.openVoiceConnection(guildId.toString(), memberVoiceChannel.id())
        connected.thenAccept {
            lavaPlayer.initialize(
                catnip.cache().voiceState(it.guildId(), catnip.selfUser()!!.id())!!.sessionId()!!,
                it.token(),
                it.endpoint()
            )
        }.join()
        return true
    }

    private fun startNext(lastTrack: AudioTrack? = null, isSkipped: Boolean = false) {
        if (lastTrack != null) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (repeatMode) {
                RepeatMode.SONG -> if (!isSkipped) {
                    lavaPlayer.play(lastTrack.makeClone())
                    return
                }
                RepeatMode.QUEUE -> {
                    queue.offer(MusicTrack(lastTrack.makeClone(), lastTrackData!!))
                }
            }
        }

        val next = queue.poll()

        if (next == null) {
            stop(MusicStopReason.QueueEnded)
            return
        }

        lastTrackData = next.data
        (lastTrackData?.source as? MusicEventSource.Discord)?.textChannel?.let { lastTextChannelId = it.idAsLong() }
        val (start, end) = next.data.trackLoadOptions.run {
            (startTimestamp ?: 0) to (endTimestamp ?: next.track.duration)
        }
        lavaPlayer.play(next.track, start, end)
        setLoadOptions(next.data)
    }

    private fun setLoadOptions(data: TrackData) {
        val (source, options) = data
        options.apply {
            volume?.let {
                publish(ChangeVolumeEvent(source, it))
            }
            repeatMode?.let {
                publish(ChangeRepeatModeEvent(source, it))
            }
        }
    }

    private fun stop(reason: MusicStopReason) {
        publish(
            when (reason) {
                is MusicStopReason.UserCommand -> MusicEndedEvent(this, reason.source, reason)
                else -> MusicEndedEvent(this, MusicEventSource.MusicSystem, reason)
            }
        )

        lavaPlayer.stop()
        destroyed = true
        queue.clear()
        catnip.closeVoiceConnection(guildId.toString())
        lavaPlayer.destroy()

        //gc
        lastTrackData = null
        voteMap.clear()
        internalClose()
    }
}
