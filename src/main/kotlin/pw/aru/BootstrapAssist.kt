package pw.aru

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import io.github.classgraph.ClassGraph
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
import pw.aru.Aru.*
import pw.aru.commands.games.manager.GameManager
import pw.aru.core.CommandProcessor
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommandProvider
import pw.aru.core.config.AruConfig
import pw.aru.core.hypervisor.AruHypervisor
import pw.aru.core.hypervisor.DevHypervisor
import pw.aru.core.hypervisor.MainHypervisor
import pw.aru.core.hypervisor.PatreonHypervisor
import pw.aru.core.listeners.*
import pw.aru.core.logging.DiscordLogBack
import pw.aru.core.music.MusicManager
import pw.aru.db.AruDB
import pw.aru.exported.aru_version
import pw.aru.libs.kodein.jit.installJit
import pw.aru.libs.kodein.jit.jitInstance
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.TaskManager
import pw.aru.utils.TaskManager.task
import pw.aru.utils.TaskType
import pw.aru.utils.caches.URLCache
import pw.aru.utils.extensions.classOf
import pw.aru.utils.extensions.listener
import pw.aru.utils.extensions.random
import pw.aru.utils.extensions.shardManager
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

fun startBootstrap() {
    Locale.setDefault(Locale("en", "US"))

    //TerminalConsoleAdaptor.initializeTerminal()

    try {
        start()
    } catch (e: Exception) {
        DiscordLogBack.disable()
        log.error("Error during load!", e)
        log.error("Impossible to continue, aborting...")
        System.exit(-1)
    }
}

internal fun createShardManager(
    injector: Kodein,
    token: String,
    onAllShardsReady: (ShardManager) -> Unit
): ShardManager {
    return shardManager {
        setToken(token)
        setAutoReconnect(true)
        setAudioEnabled(true)
        setCorePoolSize(5)
        val quote = "Aru! is loading up..."
        setGame(playing(quote))
        setStatus(OnlineStatus.DO_NOT_DISTURB)

        addEventListeners(
            listener<ReadyEvent> { event ->
                val shardManager = event.jda.asBot().shardManager

                if (shardManager.shardCache.all { it.status == LOADING_SUBSYSTEMS || it.status == CONNECTED }) {
                    shardManager.removeEventListener(this)
                    onAllShardsReady(shardManager)
                } else {
                    val online =
                        shardManager.shardCache.filter { it.status == LOADING_SUBSYSTEMS || it.status == CONNECTED }
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
        injector.direct.instance<MusicManagerListener>(),
        injector.direct.instance<HypervisorListener>()
    )

    shardManager.shardCache.forEach { it.presence.status = OnlineStatus.ONLINE }

    TaskManager.task(1, TimeUnit.MINUTES) {
        shardManager.shards.forEach {
            it.presence.game = playing("${Aru.prefixes[0]}help | ${Aru.splashes.random()} [${it.shardInfo.shardId}]")
        }
    }
}

internal fun enableDiscordLogBack(config: AruConfig) {
    DiscordLogBack.enable(config.consoleWebhook)
}

internal fun createInitialInjector(config: AruConfig, aru: Aru): Kodein {
    return Kodein {
        // Install JIT Module
        installJit()

        // Self-references
        bind<Kodein>() with singleton { kodein }
        bind<DKodein>() with singleton { dkodein }

        // Instances
        bind<Future<ShardManager>>() with instance(CompletableFuture())
        bind<AruConfig>() with instance(config)
        bind<Aru>() with instance(aru)
        bind<AruDB>() with singleton { AruDB("redis://redis:6379") }
        bind<AruHypervisor>() with when (aru) {
            MAIN -> eagerSingleton { MainHypervisor(instance()) }
            DEV -> eagerSingleton { DevHypervisor(instance()) }
            PATREON -> eagerSingleton { PatreonHypervisor(instance()) }
        }
        bind<CommandRegistry>() with singleton { CommandRegistry(instance()) }
        bind<CommandProcessor>() with singleton { CommandProcessor(instance(), instance(), instance()) }
        bind<EventWaiter>() with singleton { EventWaiter(TaskManager.scheduler(TaskType.BUNK), false) }
        bind<ReloadableListProvider>() with singleton { ReloadableListProvider() }

        // APIs
        bind<OkHttpClient>() with singleton { OkHttpClient() }

        bind<Weeb4J>() with singleton {
            Weeb4J.Builder()
                .setToken(TokenType.WOLKE, config.wshToken)
                .setHttpClient(instance())
                .setBotInfo(aru.botName, aru_version, aru.environment)
                .build()
        }

        bind<URLCache>() with singleton { URLCache(instance(), File("url_cache")) }
    }
}

internal fun createFullInjector(injector: Kodein, shardManager: ShardManager): Kodein {
    (injector.direct.instance<Future<ShardManager>>() as CompletableFuture<ShardManager>).complete(shardManager)

    return Kodein {
        //Load initial
        extend(injector)

        // Override Self-references
        bind<Kodein>(overrides = true) with singleton { kodein }
        bind<DKodein>(overrides = true) with singleton { dkodein }

        // Instances
        bind<ShardManager>() with instance(shardManager)

        // Managers
        bind<MusicManager>() with singleton { MusicManager(shardManager, instance(), instance()) }
        bind<GameManager>() with singleton { GameManager(dkodein) }

    }
}

internal fun executePostLoad(registry: CommandRegistry) {
    registry.lookup.keys.forEach {
        if (it is ICommand.PostLoad) {
            EventListeners.submitTask("PostLoad:${it.javaClass.simpleName}", it::postLoad)
        }
    }
}

internal fun computeReflectionsScan(basePackage: String): ReflectionsResult {
    val scanResult = ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .whitelistPackages(basePackage)
        .scan()

    val commands = scanResult.getClassesImplementing("pw.aru.core.commands.ICommand")
        .filter { it.hasAnnotation("pw.aru.core.commands.Command") }
        .loadClasses(ICommand::class.java)

    val commandProviders = scanResult.getClassesImplementing("pw.aru.core.commands.ICommandProvider")
        .filter { it.hasAnnotation("pw.aru.core.commands.CommandProvider") }
        .loadClasses(ICommandProvider::class.java)

    scanResult.close()

    return ReflectionsResult(commands, commandProviders)
}

internal data class ReflectionsResult(
    //Scans
    val commandScan: MutableList<Class<ICommand>>,

    //Command Providers
    val commandProviders: MutableList<Class<ICommandProvider>>
)

internal fun initCommands(injector: DKodein, registry: CommandRegistry, commands: Set<Class<out ICommand>>) {
    commands.forEach {
        try {
            val meta = it.getAnnotation(classOf<Command>())
            val command = injector.jitInstance(it)

            registry.register(meta.value.toList(), command)
        } catch (e: Exception) {
            println("$it\n$e")
        }
    }
}

internal fun initProviders(injector: DKodein, registry: CommandRegistry, commands: Set<Class<out ICommandProvider>>) {
    commands.forEach {
        try {
            injector.jitInstance(it).provide(registry)
        } catch (e: Exception) {
            println("$it\n$e")
        }
    }
}

internal fun createPlaceholderCommands(registry: CommandRegistry, commands: Set<Class<out ICommand>>) {
    for (command in commands) {
        registry.registerPlaceholder(command.getAnnotation(classOf<Command>()).value.toList())
    }
}

internal fun replacePlaceholderCommands(
    injector: DKodein,
    registry: CommandRegistry,
    commands: Set<Class<out ICommand>>
) {
    commands.forEach {
        try {
            val meta = it.getAnnotation(classOf<Command>())
            val command = injector.jitInstance(it)

            registry.register(meta.value.toList(), command)
        } catch (e: Exception) {
            println("$it\n$e")
        }
    }
}

internal fun launchRedisCheckThread(db: AruDB) {
    task(1, TimeUnit.MINUTES) {
        if (!db.isConnected) {
            log.warn("Redis Server offline! Please put it back up!")
        }
    }
}