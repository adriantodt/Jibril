package pw.aru.bot.music

import com.mewna.catnip.entity.guild.Guild
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.OrderedExecutor
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.music.entities.ItemSource
import pw.aru.bot.music.entities.ItemSource.*
import pw.aru.bot.music.events.InputMusicEvent
import pw.aru.db.AruDB
import pw.aru.libs.andeclient.entities.AndeClient
import pw.aru.libs.andeclient.events.AndePlayerEvent
import pw.aru.libs.andeclient.util.AudioTrackManager
import pw.aru.libs.eventpipes.EventPipes
import pw.aru.libs.eventpipes.api.EventExecutor
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newCachedThreadPool

class MusicSystem(override val kodein: Kodein) : KodeinAware {
    val andeClient: AndeClient by instance()
    val db: AruDB by instance()

    val players = ConcurrentHashMap<Long, MusicPlayer>()

    private val playerOrderedExecutor = OrderedExecutor(
        newCachedThreadPool(threadGroupBasedFactory("MusicPlayerOrderedExecutor"))
    )
    val pipeExecutor = EventExecutor.upgradeKeyed { key, runnable -> playerOrderedExecutor.submit(key, runnable) }
    val playerEventPipe = EventPipes.newAsyncKeyedPipe<Long, AndePlayerEvent>(pipeExecutor)
    val playerInputPipe = EventPipes.newAsyncKeyedPipe<Long, InputMusicEvent>(pipeExecutor)

    private val defaultPlayerSources = AudioTrackManager()
    private val patreonPlayerSources = AudioTrackManager()
    private val httpSafePlayerSource = AudioTrackManager()
    private val devPlayerSources = AudioTrackManager()

    val sources: Map<ItemSource, AudioPlayerManager>

    private val discordListener = MusicEventReactor(db)
    //private val dashboardListener = OutputMusicEventPublisher(db.io())

    operator fun get(guild: Guild): MusicPlayer = players.computeIfAbsent(guild.idAsLong()) { setupPlayer(guild) }

    private fun setupPlayer(guild: Guild): MusicPlayer {
        return MusicPlayer(this, guild).apply {
            subscribe(discordListener)
            //subscribe(dashboardListener)
        }
    }

    init {
        andeClient.on {
            if (it is AndePlayerEvent) {
                playerEventPipe.publish(it.guildId(), it)
            }
        }

        andeClient.newNode()
            .host(Aru.EnvVars.ANDESITE_HOSTNAME)
            .create()

        YoutubeAudioSourceManager().apply {
            setPlaylistPageCount(4)

            configureRequests {
                RequestConfig.copy(it).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()
            }
        }.register(defaultPlayerSources)

        YoutubeAudioSourceManager().apply {
            setPlaylistPageCount(128)

            configureRequests {
                RequestConfig.copy(it).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()
            }
        }.register(patreonPlayerSources, devPlayerSources)

        SoundCloudAudioSourceManager()
            .apply { queue { updateClientId() } }
            .register(defaultPlayerSources, patreonPlayerSources, devPlayerSources)
        BandcampAudioSourceManager()
            .register(defaultPlayerSources, patreonPlayerSources, devPlayerSources)
        TwitchStreamAudioSourceManager()
            .register(defaultPlayerSources, patreonPlayerSources, devPlayerSources)
        BeamAudioSourceManager()
            .register(defaultPlayerSources, patreonPlayerSources, devPlayerSources)

        HttpAudioSourceManager()
            .register(httpSafePlayerSource, devPlayerSources)

        sources = mapOf(
            DEFAULT to defaultPlayerSources,
            PATREON to patreonPlayerSources,
            HTTP_SAFE to httpSafePlayerSource,
            DEV to devPlayerSources
        )
    }

    private fun <T : AudioSourceManager> T.register(vararg managers: AudioPlayerManager) = apply {
        for (manager in managers) manager.registerSourceManager(this)
    }
}