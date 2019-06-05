package pw.aru.core.music

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
import pw.aru.Bootstrap
import pw.aru.core.music.entities.ItemSource
import pw.aru.core.music.entities.ItemSource.*
import pw.aru.db.AruDB
import pw.aru.lib.eventpipes.api.EventExecutor
import pw.aru.lib.eventpipes.internal.DefaultKeyedEventPipe
import pw.aru.libs.andeclient.entities.AndeClient
import pw.aru.libs.andeclient.events.AndePlayerEvent
import pw.aru.libs.andeclient.util.AudioTrackManager
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors.newCachedThreadPool

class MusicSystem(val andeClient: AndeClient, val db: AruDB) {

    val players = ConcurrentHashMap<Long, MusicPlayer>()

    private val playerOrderedExecutor = OrderedExecutor(
        newCachedThreadPool(threadGroupBasedFactory("MusicPlayerOrderedExecutor"))
    )
    val pipeExecutor = EventExecutor.upgradeKeyed { key, runnable -> playerOrderedExecutor.submit(key, runnable) }
    val playerEventPipe = DefaultKeyedEventPipe<Long, AndePlayerEvent>(pipeExecutor)

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
            .host(if (Bootstrap.dev) "localhost" else "andesite")
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