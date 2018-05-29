@file:JvmName("Bootstrap")
@file:JvmMultifileClass

package jibril

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import jibril.Jibril.bootQuotes
import jibril.core.CommandRegistry
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.MusicManager
import jibril.data.config.Config
import jibril.logging.DiscordLogBack
import jibril.logging.TerminalConsoleAdaptor
import jibril.utils.api.DiscordBotsPoster
import jibril.utils.extensions.*
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Game.playing
import okhttp3.OkHttpClient
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.jit.jit
import org.kodein.di.jit.jitModule
import org.reflections.Reflections
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>) {
    Locale.setDefault(Locale("en", "US"))

    TerminalConsoleAdaptor.initializeTerminal()

    try {
        start()
    } catch (e: Exception) {
        DiscordLogBack.disable()
        log.error("Error during load!", e)
        log.error("Impossible to continue, aborting...")
        System.exit(-1)
    }
}

internal fun createShardManager(token: String): ShardManager {
    return shardManager {
        setToken(token)
        setAutoReconnect(true)
        setAudioEnabled(true)
        setCorePoolSize(5)
        setGame(playing(bootQuotes.random()))
    }
}

internal fun enableDiscordLogBack(shardManager: ShardManager, config: Config) {
    DiscordLogBack.enable(
        shardManager
            .getShardForGuild(config.channels.guild!!)
            .blockUntil(JDA.Status.CONNECTED)
            .getTextChannelById(config.channels.logging!!)
    )
}

internal fun createInjector(shardManager: ShardManager, config: Config): Kodein {
    return Kodein {
        //JIT Module
        import(jitModule)

        // Instances
        bind<ShardManager>() with instance(shardManager)
        bind<Config>() with instance(config)
        bind<EventWaiter>() with singleton { EventWaiter() }

        // Managers
        bind<MusicManager>() with singleton { MusicManager(shardManager) }

        // APIs
        bind<OkHttpClient>() with singleton { OkHttpClient() }

        bind<DiscordBotsAPI>() with singleton {
            DiscordBotsAPI.Builder()
                .setToken(config.tokens.discordBots!!)
                .setHttpClient(instance())
                .build()
        }

        bind<DiscordBotsPoster>() with eagerSingleton {
            if (config.dev) DiscordBotsPoster.Dummy
            else DiscordBotsPoster.APIImpl(shardManager, instance())
        }
    }
}

internal fun computeReflectionsScan(basePackage: String): ReflectionsResult {
    val reflections = Reflections(basePackage)

    val commandScan = reflections
        .getSubTypesOf(classOf<ICommand>())
        .filterTo(LinkedHashSet()) { it.isAnnotationPresent(classOf<Command>()) }

    return ReflectionsResult(reflections, commandScan)
}

internal data class ReflectionsResult(
    //Reflections
    val reflections: Reflections,

    //Scans
    val commandScan: Set<Class<out ICommand>>
)

internal fun initCommands(injector: DKodein, commandScan: Set<Class<out ICommand>>) {
    with(injector.jit) {
        commandScan.forEach {
            val meta = it.getAnnotation(classOf<Command>())
            val command = newInstance(it)

            CommandRegistry.register(meta, command)
        }
    }
}