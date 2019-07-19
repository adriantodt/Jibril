package pw.aru.core.music

import com.mewna.catnip.entity.channel.TextChannel
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.shard.DiscordEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import mu.KLogging
import pw.aru.core.commands.help.prefix
import pw.aru.core.music.entities.*
import pw.aru.core.music.events.*
import pw.aru.core.music.events.StopMusicEvent.Reason.*
import pw.aru.core.music.internal.LavaplayerLoadResult
import pw.aru.core.music.internal.OutputMusicEventAdapter
import pw.aru.core.music.utils.NowPlayingEmbed.musicLength
import pw.aru.core.patreon.Patreon
import pw.aru.core.reporting.ErrorReporter
import pw.aru.db.AruDB
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.discordapp.safeUserInput
import pw.aru.utils.extensions.lang.awaitAll
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
        }.thenAccept { m ->
            val indices = dialog.tracks.indices.map { i -> "${i + 1}\u20E3" }

            indices.map(m::react).awaitAll().thenRun { m.react(X) }

            m.catnip().observe(DiscordEvent.MESSAGE_REACTION_ADD)
                .filter { e ->
                    e.channelId() == m.channelId() &&
                            e.messageId() == m.id() &&
                            e.userId() == author.id() &&
                            (e.emoji().name() == X || indices.contains(e.emoji().name()))
                }
                .firstElement()
                .timeout(30, TimeUnit.SECONDS) {
                    it.onComplete()
                    channel.sendMessage("$DISAPPOINTED Music choice canceled!")

                    if (player.currentTrack == null) {
                        player.publish(StopMusicEvent(MusicEventSource.MusicSystem, MUSIC_SELECTION_CANCELLED))
                    }
                }
                .subscribe { e ->
                    if (e.emoji().name() == X) {
                        channel.sendMessage("$DISAPPOINTED Music choice canceled!")
                        m.delete()

                        if (player.currentTrack == null) {
                            player.publish(StopMusicEvent(MusicEventSource.MusicSystem, MUSIC_SELECTION_CANCELLED))
                        }
                    } else {
                        val selected = dialog.tracks[indices.indexOf(e.emoji().name())]
                        m.delete()
                        player.publish(EnqueueTrackEvent(source, selected, trackLoadOptions))
                    }
                }
        }
    }

    override fun onTrackQueuedEvent(event: TrackQueuedEvent) {
        event.player.textChannel?.run {
            val who = when (event.source) {
                is MusicEventSource.Dashboard -> "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard,"
                is MusicEventSource.Discord -> "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**"
                else -> {
                    logImpossibleSource(event)
                    throw IllegalStateException("wtf event source ${event.source}")
                }
            }

            val what = "`${event.track.info.title}` (${musicLength(event.track.info.length)})"

            val where = when (event.track.data.trackLoadOptions.enqueueLoadMode) {
                EnqueueLoadMode.DEFAULT -> "to the queue"
                EnqueueLoadMode.NOW -> "to the start of the queue"
                EnqueueLoadMode.NEXT -> "to the start of the queue"
            }

            sendMessage("$THUMBSUP1 $who added $what to $where.")
        }
    }

    override fun onPlaylistQueuedEvent(event: PlaylistQueuedEvent) {
        event.player.textChannel?.run {
            val who = when (event.source) {
                is MusicEventSource.Dashboard -> "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard,"
                is MusicEventSource.Discord -> "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**"
                else -> {
                    logImpossibleSource(event)
                    throw IllegalStateException("wtf event source ${event.source}")
                }
            }

            val length = event.playlist.tracks.asSequence().map { it.info.length }.sum()
            val what = "`${event.playlist.name}` (${musicLength(length)})"

            val where = when (event.trackLoadOptions.enqueueLoadMode) {
                EnqueueLoadMode.DEFAULT -> "to the queue"
                EnqueueLoadMode.NOW -> "to the start of the queue"
                EnqueueLoadMode.NEXT -> "to the start of the queue"
            }

            sendMessage("$THUMBSUP1 $who added $what to $where.")
        }
    }

    override fun onConnectErrorEvent(event: ConnectErrorEvent) {
        val source = event.source as? MusicEventSource.Discord ?: return

        source.textChannel.run {
            when (event.error) {
                ConnectionErrorType.MEMBER_NOT_CONNECTED -> {
                    sendMessage(
                        "$X **${source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, I can't play music if you're not connected to any channel!"
                    )
                }
                ConnectionErrorType.BOT_CONNECTED_TO_OTHER_CHANNEL -> {
                    sendMessage(
                        "$X **${source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, I'm already connected to ${event.player.voiceChannel?.name()}!"
                    )
                }
                ConnectionErrorType.MEMBER_CHANNEL_FULL -> {
                    sendMessage(
                        "$X **${source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, the channel you're connected to is full!"
                    )
                }
                ConnectionErrorType.BOT_CANT_CONNECT -> {
                    sendMessage(
                        "$X **${source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, I need permission to **Connect** and **Speak** in that voice channel so I can play music!"
                    )
                }
                ConnectionErrorType.BOT_CONNECT_TIMEOUT -> {
                    sendMessage(
                        "$X **${source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, I couldn't connect to the voice channel. Mind reporting this to my developer? (Check out `a!hangout`)"
                    )
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
                        "$VOLUME Volume set to **${event.volume}/150** by **${
                        event.source.member(event.player.guild)!!.effectiveName().safeUserInput()
                        }**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$VOLUME Volume set to **${event.volume}/150** by **${
                        event.source.member(event.player.guild)!!.effectiveName().safeUserInput()
                        }**."
                    )
                }
                else -> {
                    logImpossibleSource(event)
                    sendMessage("$VOLUME Volume set to **${event.volume}/150**.")
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
                            sendMessage("$PLAY Music paused by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard.\nType `$prefix${"resume"}` to resume the player.")
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage("$PLAY Music paused by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**.\nType `$prefix${"resume"}` to resume the player.")
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
                            sendMessage("$PLAY Music resumed by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard.")
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage("$PLAY Music resumed by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**.")
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
                        )!!.effectiveName().safeUserInput()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$SUCCESS Repeat mode set to ${event.mode.name.toLowerCase().capitalize()} by **${event.source.member(
                            event.player.guild
                        )!!.effectiveName().safeUserInput()}**."
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
                        "$THUMBSUP1 Queue was shuffled by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was shuffled by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**!"
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
                        "$THUMBSUP1 Queue was cleared by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Queue was cleared by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**!"
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
        event.player.lastMessage = null
        event.player.textChannel?.run {
            when (event.source) {
                is MusicEventSource.Dashboard -> {
                    sendMessage(
                        "$THUMBSUP1 Music was skipped by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard."
                    )
                }
                is MusicEventSource.Discord -> {
                    sendMessage(
                        "$THUMBSUP1 Music was skipped by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**!"
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
                                "$THUMBSUP1 Music was stopped by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**, on the Dashboard."
                            )
                        }
                        is MusicEventSource.Discord -> {
                            sendMessage(
                                "$THUMBSUP1 Music was stopped by **${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**!"
                            )
                        }
                    }
                }
                is MusicStopReason.LeftAlone -> {
                    sendMessage(
                        "*Seems like no one is coming... D-did I do something wrong?* $SHRUG\n I left the voice channel and stopped the queue."
                    )
                }
                is MusicStopReason.SystemReason -> {
                    when (event.reason.reason) {
                        CHANNEL_DELETED -> {
                            sendMessage(
                                if (Random.nextInt(20) == 1)
                                    "$BEG Apparently the channel I was playing got deleted. *That's so sad add me to another channel and play Despacito.*"
                                else
                                    "$BEG Apparently the channel I was playing got deleted."
                            )
                        }
                        VOICE_KICK -> {
                            sendMessage(
                                if (Random.nextInt(20) == 1)
                                    "$POUT I got kicked from the channel I was playing. *I hope I didn't upset you, I like playing music to you...*"
                                else
                                    "$POUT I got kicked from the channel I was playing."
                            )
                        }
                        BOT_SHUTTING_DOWN -> {
                            sendMessage(
                                if (Random.nextInt(20) == 1)
                                    "$BEG Sorry, but the bot is shutting down. *That's so sad can we play Despacito after I got online again?*"
                                else
                                    "$BEG Sorry, but the bot is shutting down."
                            )
                        }
                        SILENT, MUSIC_SELECTION_CANCELLED -> {
                            //nop
                        }
                    }
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
                    "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**'s vote to $action, on the Dashboard,"
                }
                is MusicEventSource.Discord -> {
                    "**${event.source.member(event.player.guild)!!.effectiveName().safeUserInput()}**'s vote to $action"
                }
                else -> {
                    logImpossibleSource(event)
                    throw IllegalStateException("wtf event source ${event.source}")
                }
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
        val lastNowPlayingSent = event.player.lastNowPlayingSent

        if ((user != null && Patreon.isPremium(db, guild, user)) || (Patreon.isGuildPremium(db, guild))) {
            if (lastNowPlayingSent + 24000 < event.timestamp) {
                event.player.sendOrUpdateNowPlaying()
            }
        }
    }
}