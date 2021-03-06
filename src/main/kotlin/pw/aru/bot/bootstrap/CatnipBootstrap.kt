package pw.aru.bot.bootstrap

import com.mewna.catnip.Catnip
import com.mewna.catnip.CatnipOptions
import com.mewna.catnip.cache.CacheFlag
import com.mewna.catnip.entity.user.Presence
import com.mewna.catnip.entity.user.Presence.Activity
import com.mewna.catnip.entity.user.Presence.OnlineStatus.DND
import com.mewna.catnip.entity.user.Presence.of
import com.mewna.catnip.shard.DiscordEvent
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import mu.KLogging
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.Aru.Bot.aru
import pw.aru.Aru.EnvVars.SERVERS_WEBHOOK
import pw.aru.bot.GuildLogger
import pw.aru.bot.listeners.CommandListener
import pw.aru.utils.KodeinExtension

class CatnipBootstrap {
    companion object : KLogging()

    var onFirstShardReady: () -> Unit = {}
    var onAllShardsReady: (Int) -> Unit = {}

    fun create(): Catnip {
        return Catnip.catnip(
            CatnipOptions(Aru.EnvVars.BOT_TOKEN)
                .memberChunkTimeout(300000L)
                .cacheFlags(setOf(CacheFlag.DROP_GAME_STATUSES)) // I guess we don't need it
                .presence(of(DND, Activity.of("${aru.botName} is booting up...", Presence.ActivityType.PLAYING))),
            Vertx.vertx(
                VertxOptions()
                    .setEventLoopPoolSize(8)
                    .setAddressResolverOptions(
                        AddressResolverOptions().addServer("8.8.8.8")
                    )
            )
        )
    }

    fun configure(catnip: Catnip, kodein: Kodein) {
        val instance by kodein.instance<CommandListener>()

        catnip.loadExtension(KodeinExtension(kodein))

        catnip.on(DiscordEvent.MESSAGE_CREATE, instance)

        val shardCount by lazy { catnip.gatewayInfo()!!.shards() }
        var ready = 0

        catnip.on(DiscordEvent.READY) {
            if (ready == 0) {
                //queue("onFirstShardReady", onFirstShardReady)
                onFirstShardReady()
            }

            if (++ready == shardCount) {
                //queue("onAllShardsReady") { onAllShardsReady(shardCount) }
                onAllShardsReady(shardCount)
            }
        }

        val guildLogger = GuildLogger(SERVERS_WEBHOOK)
        catnip.on(DiscordEvent.GUILD_CREATE, guildLogger::onGuildJoin)
        catnip.on(DiscordEvent.GUILD_DELETE, guildLogger::onGuildLeave)

        catnip.connect()
    }

}