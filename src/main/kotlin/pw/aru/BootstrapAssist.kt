@file:JvmName("Bootstrap")
@file:JvmMultifileClass

package pw.aru

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA.Status.CONNECTED
import net.dv8tion.jda.core.JDA.Status.LOADING_SUBSYSTEMS
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game.playing
import net.dv8tion.jda.core.events.ReadyEvent
import okhttp3.OkHttpClient
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.reflections.Reflections
import pw.aru.Aru.bootQuotes
import pw.aru.core.CommandProcessor
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.listeners.*
import pw.aru.core.music.MusicManager
import pw.aru.data.config.AruConfig
import pw.aru.db.AruDB
import pw.aru.kodein.jit.installJit
import pw.aru.kodein.jit.jit
import pw.aru.logging.DiscordLogBack
import pw.aru.logging.TerminalConsoleAdaptor
import pw.aru.utils.TaskManager
import pw.aru.utils.TaskManager.task
import pw.aru.utils.TaskType
import pw.aru.utils.api.DiscordBotsPoster
import pw.aru.utils.extensions.classOf
import pw.aru.utils.extensions.random
import pw.aru.utils.extensions.shardManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.collections.LinkedHashMap
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

internal fun createShardManager(injector: Kodein, token: String, onAllShardsReady: (ShardManager) -> Unit): ShardManager {
    return shardManager {
        setToken(token)
        setAutoReconnect(true)
        setAudioEnabled(true)
        setCorePoolSize(5)
        val quote = bootQuotes.random()
        setGame(playing(quote))
        setStatus(OnlineStatus.DO_NOT_DISTURB)

        addEventListeners(
            listener<ReadyEvent> {
                val shardManager = it.jda.asBot().shardManager

                if (shardManager.shardCache.all { it.status == LOADING_SUBSYSTEMS || it.status == CONNECTED }) {
                    shardManager.removeEventListener(this)
                    onAllShardsReady(shardManager)
                } else {
                    val online = shardManager.shardCache.filter { it.status == LOADING_SUBSYSTEMS || it.status == CONNECTED }
                    val size = shardManager.shardCache.size()

                    val game = playing("$quote (${online.size} of $size)")
                    online.forEach { it.presence.game = game }
                }
            },
            injector.direct.instance<CommandListener>(),
            injector.direct.instance<AsyncEventWaiter>()
        )
    }
}

internal fun completeShardManager(shardManager: ShardManager, injector: Kodein) {
    shardManager.addEventListener(
        injector.direct.instance<VoiceLeaveListener>(),
        injector.direct.instance<GuildListener>()
    )

    shardManager.shardCache.forEach { it.presence.status = OnlineStatus.ONLINE }

    TaskManager.task(1, TimeUnit.MINUTES) {
        shardManager.shards.forEach {
            it.presence.game = playing("${Aru.prefixes[0]}help | ${Aru.splashes.random()} [${it.shardInfo.shardId}]")
        }
    }
}

internal fun enableDiscordLogBack(shardManager: ShardManager, config: AruConfig) {
    DiscordLogBack.enable(shardManager.getTextChannelById(config.logChannel))
}

internal fun createInitialInjector(config: AruConfig): Kodein {
    return Kodein {
        // Install JIT Module
        installJit()

        // Self-references
        bind<Kodein>() with singleton { kodein }
        bind<DKodein>() with singleton { instance<Kodein>().direct }

        // Instances
        bind<Future<ShardManager>>() with instance(CompletableFuture())
        bind<AruConfig>() with instance(config)
        bind<AruDB>() with singleton { AruDB() }
        bind<CommandProcessor>() with singleton { CommandProcessor(instance()) }
        bind<EventWaiter>() with singleton { EventWaiter(TaskManager.scheduler(TaskType.BUNK), false) }

        // APIs
        bind<OkHttpClient>() with singleton { OkHttpClient() }

        bind<DiscordBotsAPI>() with singleton {
            DiscordBotsAPI.Builder()
                .setToken(config.dblToken)
                .setHttpClient(instance())
                .build()
        }
    }
}

internal fun createFullInjector(injector: Kodein, shardManager: ShardManager): Kodein {
    (injector.direct.instance<Future<ShardManager>>() as CompletableFuture<ShardManager>).complete(shardManager)

    return Kodein {
        //Load initial
        extend(injector)

        // Override Self-references
        bind<Kodein>(overrides = true) with singleton { kodein }
        bind<DKodein>(overrides = true) with singleton { instance<Kodein>().direct }

        // Instances
        bind<ShardManager>() with instance(shardManager)

        // Managers
        bind<MusicManager>() with singleton { MusicManager(shardManager, instance(), instance()) }


        bind<DiscordBotsPoster>() with eagerSingleton {
            if (instance<AruConfig>().dev) DiscordBotsPoster.Dummy
            else DiscordBotsPoster.APIImpl(shardManager, instance())
        }
    }
}

internal fun executePostLoad() {
    CommandRegistry.lookup.keys.forEach {
        if (it is ICommand.PostLoad) {
            EventListeners.submitTask("PostLoad:${it.javaClass.simpleName}", it::postLoad)
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

internal fun initCommands(injector: DKodein, commands: Set<Class<out ICommand>>) {
    with(injector.jit) {
        commands.forEach {
            try {
                val meta = it.getAnnotation(classOf<Command>())
                val command = newInstance(it)

                CommandRegistry.register(meta.value, command)
            } catch (e: Exception) {
                println("$it\n$e")
            }
        }
    }
}

internal fun createPlaceholderCommands(commands: Set<Class<out ICommand>>) {
    commands.forEach {
        val meta = it.getAnnotation(classOf<Command>())
        val req = it.getAnnotation(classOf<UseFullInjector>())

        CommandRegistry.registerPlaceholder(meta.value, req.reroute)
    }
}

internal fun replacePlaceholderCommands(injector: DKodein, commands: Set<Class<out ICommand>>): List<() -> Unit> {
    //Used to create the commands
    val jit = injector.jit

    //Grouping
    val map = LinkedHashMap<Long, MutableList<() -> Unit>>()

    for (it in commands) {
        try {
            //Get annotation and instantiate the command
            val meta = it.getAnnotation(classOf<Command>())
            val command = jit.newInstance(it)

            for ((event, args) in CommandRegistry.registerOverride(meta.value, command)) {
                map.getOrPut(event.guild.idLong, ::ArrayList) += { command.call(event, args) }
            }
        } catch (e: Exception) {
            println("$it\n$e")
        }
    }

    return map.values.map { { it.forEach { it() } } }
}

internal fun launchRedisCheckThread(db: AruDB) {
    task(1, TimeUnit.MINUTES) {
        if (!db.isConnected) {
            log.warn("Redis Server offline! Please put it back up!")
        }
    }
}