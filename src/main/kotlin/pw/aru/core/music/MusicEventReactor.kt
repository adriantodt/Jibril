package pw.aru.core.music

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.shard.DiscordEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gg.amy.catnip.utilities.FutureUtil
import gg.amy.catnip.utilities.waiter.EventExtension
import mu.KLogging
import pw.aru.core.commands.help.prefix
import pw.aru.core.music.entities.*
import pw.aru.core.music.events.*
import pw.aru.core.music.internal.LavaplayerLoadResult
import pw.aru.core.music.internal.OutputMusicEventAdapter
import pw.aru.core.music.utils.NowPlayingEmbed.musicLength
import pw.aru.core.patreon.Patreon
import pw.aru.core.reporting.ErrorReporter
import pw.aru.db.AruDB
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.lang.toStringReflexively
import pw.aru.utils.extensions.lib.sendEmbed
import pw.aru.utils.text.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MusicEventReactor(private val db: AruDB) : OutputMusicEventAdapter() {

    companion object : KLogging() {
        fun logImpossibleSource(event: OutputMusicEvent) {
            ErrorReporter()
                .exception(IllegalStateException("Theoretically impossible state=${event.source} for $event"))
                .extra("event", event.toStringReflexively())
                .logTimestamp()
                .errorIdFromContext()
                .report()
                .logToFile()
                .logAsError()
        }

        const val dialogMessage =
            "React with the specified value to start playing the music.\n(or with $X to cancel the selection)\n\n"
    }

    override fun onLoadResultsEvent(event: LoadResultsEvent) {
        if (event.source is MusicEventSource.Discord) {
            val member = event.source.member(event.player.guild)!!
            val channel = event.source.channel(event.player.guild)!!
            val results = event.results

            when (results) {
                is LavaplayerLoadResult.NoMatches -> {
                    channel.sendMessage("$ERROR Nothing found!")
                }
                is LavaplayerLoadResult.Failed -> {
                    if (results.exception.severity == FriendlyException.Severity.COMMON) {
                        channel.sendMessage(
                            "$DISAPPOINTED Aw, I can't play this song. I think it's due to copyright issues."
                        )
                    } else {
                        channel.sendMessage(
                            "$ERROR Uh... I got an error while loading this song. Do you mind trying again, please?"
                        )
                    }
                }
                else -> {
                    chooseDialog(event.player, channel, member, results, event.source, event.trackLoadOptions)
                }
            }
        }
    }

    private fun chooseDialog(
        player: MusicPlayer,
        channel: TextChannel,
        author: Member,
        result: LavaplayerLoadResult,
        source: MusicEventSource,
        trackLoadOptions: TrackLoadOptions
    ) {
        data class DialogRender(
            val title: String,
            val thumbnail: String,
            val tracks: List<AudioTrack>
        )

        val dialog = when (result) {
            is LavaplayerLoadResult.SearchResults -> DialogRender(
                result.results.name,
                "https://assets.aru.pw/img/searchresults.png",
                result.results.tracks.take(5)
            )
            is LavaplayerLoadResult.Playlist -> DialogRender(
                "Choose the music to play:",
                "https://assets.aru.pw/img/playlist.png",
                result.playlist.tracks.take(5)

            )
            is LavaplayerLoadResult.Track -> DialogRender(
                "Choose the music to play:",
                "https://assets.aru.pw/img/playlist.png",
                listOf(result.track)
            )
            else -> throw IllegalStateException("result is $result")
        }

        channel.sendEmbed {
            title(dialog.title)
            thumbnail(dialog.thumbnail)
            color(AruColors.primary)

            description(
                dialog.tracks.withIndex().joinToString(prefix = dialogMessage, separator = "\n") { (index, it) ->
                    "${index + 1}\u20E3 **[${it.info.title}](${it.info.uri}) (${musicLength(it.info.length)})**"
                }
            )
        }.thenAccept {
            val indices = dialog.tracks.indices.map { i -> "${i + 1}\u20E3" }

            FutureUtil.awaitAll(indices.map(it::react)).thenRun { it.react(X) }

            it.catnip().extension(EventExtension::class.java)!!
                .waitForEvent(DiscordEvent.MESSAGE_REACTION_ADD)
                .condition { e ->
                    e.channelId() == it.channelId() &&
                            e.messageId() == it.id() &&
                            e.userId() == author.id() &&
                            (e.emoji().name() == X || indices.contains(e.emoji().name()))
                }
                .timeout(30, TimeUnit.SECONDS) {
                    channel.sendMessage("$DISAPPOINTED Music choice canceled!")

                    if (player.currentTrack == null) {
                        player.publish(StopMusicEvent(MusicEventSource.MusicSystem))
                    }
                }
                .action { e ->
                    if (e.emoji().name() == X) {
                        channel.sendMessage("$DISAPPOINTED Music choice canceled!")

                        if (player.currentTrack == null) {
                            player.publish(StopMusicEvent(MusicEventSource.MusicSystem))
                        }
                    } else {
                        val selected = dialog.tracks[indices.indexOf(e.emoji().name())]
                        it.delete()
                        player.publish(EnqueueTrackEvent(source, selected, trackLoadOptions))
                    }
                }
        }
    }

    override fun onTrackQueuedEvent(event: TrackQueuedEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    TODO("onTrackQueuedEvent: source is Dashboard")
                }
                is MusicEventSource.Discord -> {
                    TODO("onTrackQueuedEvent: source is Discord")
                }
                else -> {
                    logImpossibleSource(event)
                    TODO("onTrackQueuedEvent: source is impoossible")
                }
            }
        }
    }

    override fun onPlaylistQueuedEvent(event: PlaylistQueuedEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    TODO("onPlaylistQueuedEvent: source is Dashboard")
                }
                is MusicEventSource.Discord -> {
                    TODO("onPlaylistQueuedEvent: source is Discord")
                }
                else -> {
                    logImpossibleSource(event)
                    TODO("onPlaylistQueuedEvent: source is impossible")
                }
            }
        }
    }

    override fun onConnectErrorEvent(event: ConnectErrorEvent) {
        event.source.channel(event.player.guild)?.run {
            sendMessage(event.error.toString())
            when (event.error) {
                ConnectionErrorType.MEMBER_NOT_CONNECTED -> {
                    TODO("onConnectErrorEvent: MEMBER_NOT_CONNECTED")
                }
                ConnectionErrorType.BOT_CONNECTED_TO_OTHER_CHANNEL -> {
                    TODO("onConnectErrorEvent: BOT_CONNECTED_TO_OTHER_CHANNEL")
                }
                ConnectionErrorType.MEMBER_CHANNEL_FULL -> {
                    TODO("onConnectErrorEvent: MEMBER_CHANNEL_FULL")
                }
                ConnectionErrorType.BOT_CANT_CONNECT -> {
                    TODO("onConnectErrorEvent: BOT_CANT_CONNECT")
                }
            }
        }
    }

    override fun onMusicStartedEvent(event: MusicStartedEvent) {
    }

    override fun onChangedVolumeEvent(event: ChangedVolumeEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$VOLUME Volume set to **${event.player.lavaPlayer.volume()}/150** by **${event.source.member(
                            event.player.guild
                        )!!.effectiveName()}**, on the Dashboard.."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$VOLUME Volume set to **${event.player.lavaPlayer.volume()}/150** by **${event.source.member(
                            event.player.guild
                        )!!.effectiveName()}**, on the Dashboard.."
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage("$VOLUME Volume set to **${event.player.lavaPlayer.volume()}/150**.")
                }
            }
        }
    }

    override fun onChangedPauseStateEvent(event: ChangedPauseStateEvent) {
        event.player.textChannel?.run {
            when (event.state) {
                PauseState.PAUSED -> {
                    when (event.source) {
                        is MusicEventSource.Dashboard -> {
                            sendMessage("$PLAY Music paused by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard.\nType `$prefix${"resume"}` to resume the player.")
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage("$PLAY Music paused by **${event.source.member(event.player.guild)!!.effectiveName()}**.\nType `$prefix${"resume"}` to resume the player.")
                        }
                        is MusicEventSource.VotingSystem -> {
                            sendMessage(
                                "$THUMBSUP1 _The people has spoken!_ Music paused due to enough votes!"
                            )
                        }
                        else -> {
                            logImpossibleSource(event)
                            sendMessage("$PLAY Music paused.\nType `$prefix${"resume"}` to resume the player.")
                        }
                    }

                }
                PauseState.RESUMED -> {
                    when (event.source) {
                        is MusicEventSource.Dashboard -> {
                            sendMessage("$PLAY Music resumed by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard.")
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage("$PLAY Music resumed by **${event.source.member(event.player.guild)!!.effectiveName()}**.")
                        }
                        is MusicEventSource.VotingSystem -> {
                            sendMessage(
                                "$THUMBSUP1 _The people has spoken!_ Music resumed due to enough votes!"
                            )
                        }
                        else -> {
                            logImpossibleSource(event)
                            sendMessage("$PLAY Music resumed.")
                        }
                    }
                }
            }
        }
    }

    override fun onChangedRepeatModeEvent(event: ChangedRepeatModeEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$SUCCESS Repeat mode set to ${event.mode.name.toLowerCase().capitalize()} by **${event.source.member(
                            event.player.guild
                        )!!.effectiveName()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$SUCCESS Repeat mode set to ${event.mode.name.toLowerCase().capitalize()} by **${event.source.member(
                            event.player.guild
                        )!!.effectiveName()}**."
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage(
                        "$SUCCESS Repeat mode set to ${event.mode.name.toLowerCase().capitalize()}!"
                    )
                }
            }
        }
    }

    override fun onQueueShuffledEvent(event: QueueShuffledEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was shuffled by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was shuffled by **${event.source.member(event.player.guild)!!.effectiveName()}**!"
                    )
                }
                MusicEventSource.VotingSystem -> {
                    sendMessage(
                        "$THUMBSUP1 _The people has spoken!_ Queue was shuffled due to enough votes!"
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage(
                        "$THUMBSUP1 Queue shuffled."
                    )
                }
            }
        }
    }

    override fun onQueueClearedEvent(event: QueueClearedEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was cleared by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was cleared by **${event.source.member(event.player.guild)!!.effectiveName()}**!"
                    )
                }
                MusicEventSource.VotingSystem -> {
                    sendMessage(
                        "$THUMBSUP1 _The people has spoken!_ Queue was cleared due to enough votes!"
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage(
                        "$THUMBSUP1 Queue cleared."
                    )
                }
            }
        }
    }

    override fun onTrackGotStuckEvent(event: TrackGotStuckEvent) {
        event.player.textChannel?.run {
            sendMessage(
                "$ERROR The song `${event.track.info.title}` got stuck while playing. Skipping..."
            )
        }
    }

    override fun onTrackErroredEvent(event: TrackErroredEvent) {
        event.player.textChannel?.run {
            sendMessage(
                "$ERROR There was an error while playing `${event.track.info.title}`, skipping..."
            )
        }
    }

    override fun onTrackSkippedEvent(event: TrackSkippedEvent) {
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$THUMBSUP1 Music was skipped by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Music was skipped by **${event.source.member(event.player.guild)!!.effectiveName()}**!"
                    )
                }
                MusicEventSource.VotingSystem -> {
                    sendMessage(
                        "$THUMBSUP1 _The people has spoken!_ Music was skipped due to enough votes!"
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage(
                        "$THUMBSUP1 Music was skipped."
                    )
                }
            }
        }
    }

    override fun onNextTrackEvent(event: NextTrackEvent) {
        event.player.sendOrUpdateNowPlaying()
    }

    override fun onMusicEndedEvent(event: MusicEndedEvent) {
        event.player.textChannel?.run {
            when (event.reason) {
                is MusicStopReason.UserCommand -> {
                    when (event.source) {
                        MusicEventSource.AndesiteNode -> {
                            logImpossibleSource(event)
                            sendMessage(
                                "$THINKING Music was stopped by the Andesite System. I guess something went really wrong!"
                            )
                        }
                        MusicEventSource.MusicSystem -> {
                            logImpossibleSource(event)
                            sendMessage(
                                "$THINKING Music was stopped by the Music System. I guess something went really wrong!"
                            )
                        }
                        MusicEventSource.VotingSystem -> {
                            sendMessage(
                                "$THUMBSUP1 _The people has spoken!_ Music was stopped due to enough votes!"
                            )
                        }
                        is MusicEventSource.Dashboard -> {
                            sendMessage(
                                "$THUMBSUP1 Music was stopped by **${event.source.member(event.player.guild)!!.effectiveName()}**, on the Dashboard."
                            )
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage(
                                "$THUMBSUP1 Music was stopped by **${event.source.member(event.player.guild)!!.effectiveName()}**!"
                            )
                        }
                    }
                }
                is MusicStopReason.LeftAlone -> {
                    sendMessage(
                        "*Seems like no one is coming... D-did I do something wrong?* $SHRUG\n I left the voice channel and stopped the queue."
                    )
                }
                is MusicStopReason.ChannelDeleted -> {
                    sendMessage(
                        if (Random.nextInt(20) == 1)
                            "$BEG Apparently the channel I was playing got deleted. *That's so sad add me to another channel and play Despacito."
                        else
                            "$BEG Apparently the channel I was playing got deleted."
                    )
                }
                is MusicStopReason.QueueEnded -> {
                    sendMessage(
                        if (Patreon.isGuildPremium(db, event.player.guild)) {
                            "$PATREON_WINK Finished playing the queue! Hope you enjoyed it."
                        } else {
                            "$WINK Finished playing the queue! Hope you enjoyed it.\n" +
                                    "$BEG If I were a good bot, please consider donating to keep the bot alive. It's as simple as `aru!links`."
                        }
                    )
                }
                is MusicStopReason.SilentQuit -> {
                    //nop
                }
            }

        }
    }

    override fun onChangedVoteEvent(event: ChangedVoteEvent) {
        event.player.textChannel?.run {

            val action = when (event.voteType) {
                VoteType.SKIP -> "skip the music"
                VoteType.STOP -> "stop the music"
                VoteType.SHUFFLE -> "shuffle the queue"
                VoteType.PAUSE -> "pause the music"
                VoteType.RESUME -> "resume the music"
                VoteType.CLEAR_QUEUE -> "clear the queue"
            }

            val what = when (event.source) {
                is MusicEventSource.Dashboard -> {
                    "**${event.source.member(event.player.guild)!!.effectiveName()}**'s vote to $action, on the Dashboard,"
                }
                is MusicEventSource.Discord -> {
                    "**${event.source.member(event.player.guild)!!.effectiveName()}**'s vote to $action"
                }
                else -> throw IllegalStateException("wtf event source")
            }

            val happened = if (event.added) " has been added." else " has been removed."

            val result = if (event.votesLeft <= 0) "The action will be taken soon."
            else "More ${event.votesLeft} votes are required to $action."

            sendMessage("$SUCCESS $what $happened $result")
        }
    }

    override fun onListenersLeftEvent(event: ListenersLeftEvent) {
        event.player.textChannel?.run {
            when (event.state) {
                ListenersLeftState.LEFT_ALONE -> {
                    sendMessage(
                        "$POUT I was left alone in the voice channel... If no one joins within **2 minutes**, I'm going to **leave the channel and stop the queue.**"
                    )
                }
                ListenersLeftState.RETURNED -> {
                    sendMessage(
                        "$SMILE2 *Yay someone joined me to listen to some nice songs!*\n$SUCCESS I've resumed from where I stopped for you!"
                    )
                }
            }
        }
    }

    override fun onPlayerInfoEvent(event: PlayerInfoEvent) {
        val guild = event.player.guild
        val user = event.player.lastTrackData?.source?.member(guild)?.user()
        val db = event.player.musicSystem.db

        if (user != null && (Patreon.isPremium(db, guild, user)) || (Patreon.isGuildPremium(db, guild))) {
            if (event.timestamp > 0 && event.timestamp / 10000 % 30 == 0L) {
                event.player.sendOrUpdateNowPlaying()
            }
        }
    }
}