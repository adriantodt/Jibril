package pw.aru.bootstrap

import com.mewna.catnip.Catnip
import com.mewna.catnip.CatnipOptions
import com.mewna.catnip.cache.CacheFlag
import com.mewna.catnip.entity.user.Presence
import com.mewna.catnip.shard.DiscordEvent
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import mu.KLogging
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.GuildLogger
import pw.aru.bot.listeners.CommandListener
import pw.aru.utils.AruTaskExecutor
import pw.aru.utils.KodeinExtension
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class CatnipBootstrap {
    companion object : KLogging()

    fun create(): Catnip {
        return Catnip.catnip(
            CatnipOptions(Aru.EnvVars.BOT_TOKEN)
                .memberChunkTimeout(300000L)
                .cacheFlags(setOf(CacheFlag.DROP_GAME_STATUSES)) // I guess we don't need it
                .presence(
                    Presence.of(
                        Presence.OnlineStatus.DND,
                        Presence.Activity.of("Aru! is booting up...", Presence.ActivityType.PLAYING)
                    )
                ),
            Vertx.vertx(
                VertxOptions()
                    .setEventLoopPoolSize(8)
                    .setAddressResolverOptions(
                        AddressResolverOptions().addServer("8.8.8.8")
                    )
            )
        )
    }

    fun configure(catnip: Catnip, kodein: Kodein): CompletableFuture<Catnip> {
        val instance by kodein.instance<CommandListener>()

        catnip.loadExtension(KodeinExtension(kodein))

        catnip.on(DiscordEvent.MESSAGE_CREATE, instance)

        val shardCount = catnip.gatewayInfo()!!.shards()
        var ready = 0
        val allShardsReady = CompletableFuture<Unit>()
        val onAnyReady = CompletableFuture<Catnip>()
        val aru by kodein.instance<Aru>()

        catnip.on(DiscordEvent.READY) {
            onAnyReady.complete(catnip)
            if (++ready == shardCount) allShardsReady.complete(Unit)
        }

        val guildLogger = GuildLogger(catnip, Aru.EnvVars.SERVERS_WEBHOOK)
        catnip.on(DiscordEvent.GUILD_CREATE, guildLogger::onGuildJoin)
        catnip.on(DiscordEvent.GUILD_DELETE, guildLogger::onGuildLeave)

        allShardsReady.thenAcceptAsync {
            AruTaskExecutor.task(1, TimeUnit.MINUTES) {
                for (i in 0 until shardCount) catnip.presence(
                    Presence.of(
                        Presence.OnlineStatus.ONLINE,
                        Presence.Activity.of(
                            "${aru.prefixes[0]}help | ${Aru.splashes.random()} [$i]",
                            Presence.ActivityType.PLAYING
                        )
                    )
                )
            }

            logger.info { "${aru.botName} loaded!" }
        }

        return onAnyReady
    }

}