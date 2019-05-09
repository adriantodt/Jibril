package pw.aru.core.music

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.channel.VoiceChannel
import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import com.mewna.catnip.shard.DiscordEvent
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import pw.aru.core.music.entities.*
import pw.aru.core.music.events.*
import pw.aru.core.music.events.StopMusicEvent.Reason.SILENT
import pw.aru.core.music.internal.AbstractMusicPlayer
import pw.aru.core.music.internal.LavaplayerLoadHandler
import pw.aru.core.music.internal.LavaplayerLoadResult
import pw.aru.core.music.internal.TrackData
import pw.aru.core.music.utils.NowPlayingEmbed.nowPlayingEmbed
import pw.aru.libs.andeclient.events.player.PlayerUpdateEvent
import pw.aru.libs.andeclient.events.track.TrackEndEvent
import pw.aru.libs.andeclient.events.track.TrackExceptionEvent
import pw.aru.libs.andeclient.events.track.TrackStartEvent
import pw.aru.libs.andeclient.events.track.TrackStuckEvent
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.extensions.lang.getValue
import pw.aru.utils.extensions.lang.roundRobinFlatten
import pw.aru.utils.extensions.lib.component1
import pw.aru.utils.extensions.lib.component2
import pw.aru.utils.extensions.lib.component3
import pw.aru.utils.extensions.lib.humanUsersCount
import reactor.adapter.rxjava.toFlux
import reactor.core.publisher.toMono
import java.lang.System.currentTimeMillis
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MusicPlayer(
    val musicSystem: MusicSystem, guild: Guild
) : AbstractMusicPlayer(musicSystem, guild.catnip(), guild.idAsLong()) {

    private val guildId = guild.idAsLong()
    val catnip = guild.catnip()!!
    private var lastTextChannelId: Long = 0
    private var lastMessage: Message? = null

    val andePlayer = musicSystem.andeClient.newPlayer(guildId)
    var queue = LinkedBlockingDeque<MusicTrack>()
    var repeatMode = RepeatMode.NONE
    val voteMap = EnumMap<VoteType, HashSet<Long>>(VoteType::class.java)

    var lastPosition: Long = -1
    var lastTimestamp: Long = -1
    var lastTrackData: TrackData? = null
    var lastNowPlayingSent: Long = -1
    private val nowPlayingLock = ReentrantLock()

    var destroyed: Boolean = false

    val guild: Guild
        get() = catnip.cache().guild(guildId)!!

    val voiceChannel: VoiceChannel?
        get() = catnip.cache().voiceState(guildId, catnip.selfUser()!!.idAsLong())?.channel()

    val currentTrack: AudioTrack?
        get() = andePlayer.playingTrack()

    val textChannel: TextChannel?
        get() = catnip.cache().channel(guildId, lastTextChannelId)?.asTextChannel()

    fun sendOrUpdateNowPlaying(memberRequested: Member? = null, logTime: Boolean = true) {
        nowPlayingLock.withLock {
            if (lastNowPlayingSent + 1000 > currentTimeMillis()) return

            val m = lastMessage
            if (m != null) {
                m.edit(nowPlayingEmbed(this, memberRequested)).thenAccept { lastMessage = it }
            } else {
                val sentMessage by textChannel?.sendMessage(nowPlayingEmbed(this, memberRequested))
                sentMessage?.let { lastMessage = it }
            }
            if (logTime) lastNowPlayingSent = currentTimeMillis()
        }
    }

    fun sendNowPlaying(memberRequested: Member? = null) {
        val m = lastMessage

        if (m != null) {
            lastMessage = null
            try {
                m.delete()
                lastMessage = null
            } catch (_: Exception) {
            }
        }

        sendOrUpdateNowPlaying(memberRequested, false)
    }

    override fun onLoadItemEvent(event: LoadItemEvent) {
        if (!requireConnected(event.source)) return
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
                publish(TrackQueuedEvent(this, event.source, musicTrack))
                publish(SkipTrackEvent(event.source))
                return
            }
            EnqueueLoadMode.DEFAULT -> {
                queue.offerLast(musicTrack)
                publish(TrackQueuedEvent(this, event.source, musicTrack))
            }
            EnqueueLoadMode.NEXT -> {
                queue.offerFirst(musicTrack)
                publish(TrackQueuedEvent(this, event.source, musicTrack))
            }
        }

        if (andePlayer.playingTrack() == null) {
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
                publish(PlaylistQueuedEvent(this, event.source, event.playlist, event.trackLoadOptions))
                publish(SkipTrackEvent(event.source))
                return
            }
            EnqueueLoadMode.DEFAULT -> {
                tracks.forEach { queue.offerLast(it) }
                publish(PlaylistQueuedEvent(this, event.source, event.playlist, event.trackLoadOptions))
            }
            EnqueueLoadMode.NEXT -> {
                tracks.reversed().forEach { queue.offerFirst(it) }
                publish(PlaylistQueuedEvent(this, event.source, event.playlist, event.trackLoadOptions))
            }
        }

        if (andePlayer.playingTrack() == null) {
            publish(MusicStartedEvent(this, event.source))
            startNext()
        }
    }

    override fun onChangeVolumeEvent(event: ChangeVolumeEvent) {
        eagerHandle(event)

        andePlayer.controls().volume(event.volume).execute()
        publish(ChangedVolumeEvent(this, event.source, event.volume))
    }

    override fun onChangePauseStateEvent(event: ChangePauseStateEvent) {
        eagerHandle(event)

        when (event.state) {
            null -> {
                if (andePlayer.paused()) {
                    andePlayer.controls().resume().execute()
                    publish(ChangedPauseStateEvent(this, event.source, PauseState.RESUMED))
                } else {
                    andePlayer.controls().pause().execute()
                    publish(ChangedPauseStateEvent(this, event.source, PauseState.PAUSED))
                }
            }
            PauseState.PAUSED -> {
                if (!andePlayer.paused()) andePlayer.controls().pause().execute()
                publish(ChangedPauseStateEvent(this, event.source, event.state))
            }
            PauseState.RESUMED -> {
                if (andePlayer.paused()) andePlayer.controls().resume().execute()
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

        andePlayer.controls().seek(event.position).execute()
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

        val maySkip = andePlayer.playingTrack() != null
        if (maySkip) publish(TrackSkippedEvent(this, event.source))
        startNext(currentTrack, maySkip)
    }

    override fun onStopMusicEvent(event: StopMusicEvent) {
        eagerHandle(event)

        when (event.source) {
            MusicEventSource.MusicSystem -> {
                stop(MusicStopReason.SystemReason(event.reason ?: SILENT))
            }
            is MusicEventSource.Dashboard, is MusicEventSource.Discord -> {
                stop(MusicStopReason.UserCommand(event.source))
            }
            else -> throw IllegalStateException("wtf event source is ${event.source}")
        }
    }

    override fun onToggleVoteEvent(event: ToggleVoteEvent) {
        eagerHandle(event)

        val votes = voteMap.getOrPut(event.type, ::HashSet)

        val id = event.source.member(guild)?.idAsLong()
            ?: throw IllegalStateException("wtf event source is ${event.source}")

        val toAdd = !votes.contains(id)

        if (toAdd) {
            votes.add(id)
        } else {
            votes.remove(id)
        }

        val requiredVotes = (voiceChannel!!.humanUsersCount * 0.6).toInt()
        val voteCount = votes.size
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

    private val listenersLeftLock = Semaphore(1)

    override fun onDiscordListenersLeftEvent(event: DiscordListenersLeftEvent) {
        if (!listenersLeftLock.tryAcquire()) return
        publish(ListenersLeftEvent(this, ListenersLeftState.LEFT_ALONE))
        andePlayer.controls().pause().execute()

        var channelId = voiceChannel!!.idAsLong()
        val selfId = catnip.selfUser()!!.idAsLong()

        catnip.observe(DiscordEvent.VOICE_STATE_UPDATE)
            .filter {
                val (_, stateChannelId, stateUserId) = it

                when {
                    //someone joined
                    stateChannelId == channelId && stateUserId != selfId -> {
                        true
                    }
                    //bot moved to another channel (or channel deleted)
                    stateUserId == selfId && stateChannelId != channelId -> {
                        val channel = it.channel()

                        if (channel == null) {
                            true
                        } else {
                            channelId = channel.idAsLong()
                            channel.humanUsersCount > 0
                        }
                    }
                    else -> false
                }
            }
            .take(1)
            .singleElement()
            .timeout(2, TimeUnit.MINUTES) {
                if (!destroyed) {
                    stop(MusicStopReason.LeftAlone)
                }
                listenersLeftLock.release()
            }
            .subscribe {
                if (!destroyed) {
                    if (it.channel() != null) {
                        andePlayer.controls().resume().execute()
                        publish(ListenersLeftEvent(this, ListenersLeftState.RETURNED))
                    }
                }
                listenersLeftLock.release()
            }
    }

    override fun onTrackStartEvent(event: TrackStartEvent) {
        publish(NextTrackEvent(this, event.track()))
    }

    override fun onPlayerUpdateEvent(event: PlayerUpdateEvent) {
        lastPosition = event.position()
        lastTimestamp = event.timestamp()
        val currentTrack = event.player().playingTrack() ?: return
        publish(
            PlayerInfoEvent(
                this,
                lastTimestamp,
                lastPosition,
                MusicTrack(currentTrack, lastTrackData!!),
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

        if (botVoiceChannel != null) {
            if (memberVoiceChannel == botVoiceChannel) {
                return true
            }

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

        val nextVsu = catnip.observe(DiscordEvent.VOICE_SERVER_UPDATE)
            .toFlux()
            .filter { it.guildIdAsLong() == guildId }
            .toMono()
            .cache()

        catnip.openVoiceConnection(guildId, memberVoiceChannel.idAsLong())

        val vsu = nextVsu.runCatching { block(Duration.ofSeconds(45)) }.getOrNull()

        if (vsu == null) {
            publish(ConnectErrorEvent(this, source, ConnectionErrorType.BOT_CONNECT_TIMEOUT))
            catnip.closeVoiceConnection(guildId)
            return false
        }

        andePlayer.handleVoiceServerUpdate(
            catnip.cache().voiceState(vsu.guildId(), catnip.selfUser()!!.id())!!.sessionId()!!,
            vsu.token(),
            vsu.endpoint()
        )

        return true
    }

    private fun startNext(lastTrack: AudioTrack? = null, isSkipped: Boolean = false) {
        if (lastTrack != null) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (repeatMode) {
                RepeatMode.SONG -> if (!isSkipped) {
                    val (start, end) = lastTrackData!!.trackLoadOptions.run {
                        (startTimestamp ?: 0) to (endTimestamp ?: lastTrack.duration)
                    }
                    andePlayer.controls().play().track(lastTrack.makeClone()).start(start).end(end).execute()
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
        (next.data.source as? MusicEventSource.Discord)?.textChannel?.let { lastTextChannelId = it.idAsLong() }
        andePlayer.controls().play().apply {
            track(next.track)
            val (_, _, volume, _, startTimestamp, endTimestamp) = next.data.trackLoadOptions
            volume(volume)
            start(startTimestamp)
            end(endTimestamp)
        }.execute()

        next.data.trackLoadOptions.repeatMode?.let {
            publish(ChangeRepeatModeEvent(next.data.source, it))
        }

    }

    private fun stop(reason: MusicStopReason) {
        musicSystem.players.remove(guildId)

        publish(
            when (reason) {
                is MusicStopReason.UserCommand -> MusicEndedEvent(this, reason.source, reason)
                else -> MusicEndedEvent(this, MusicEventSource.MusicSystem, reason)
            }
        )

        andePlayer.controls().stop().execute()
        destroyed = true
        queue.clear()
        catnip.closeVoiceConnection(guildId.toString())
        andePlayer.destroy()

        //gc
        lastTrackData = null
        voteMap.clear()
        internalClose()
    }
}
