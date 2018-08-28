@file:JvmName("Bootstrap")

package pw.aru

import mu.KotlinLogging.logger
import org.kodein.di.direct
import org.kodein.di.generic.instance
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.config.ConfigManager
import pw.aru.core.listeners.EventListeners.queueTask
import pw.aru.core.listeners.EventListeners.submitTask
import pw.aru.utils.TaskManager.queue
import pw.aru.utils.TaskType.BUNK
import pw.aru.utils.api.DBLPoster
import pw.aru.utils.api.DBotsPoster
import pw.aru.utils.extensions.classOf
import pw.aru.utils.extensions.invoke
import pw.aru.utils.helpers.AsyncInfoMonitor

val log = logger("pw.aru.Bootstrap")

fun main(args: Array<String>) = startBootstrap()

internal fun start() {
    // Start-up AsyncInfoMonitor
    AsyncInfoMonitor()
    log.info("AruBot starting...")

    // Compute Reflections Scan async
    val scanTask = submitTask("ReflectionsScanTask") {
        computeReflectionsScan(basePackage = "pw.aru")
    }

    val config = ConfigManager.config
    enableDiscordLogBack(config)

    Aru.prefixes += config.prefixes.split(',')

    //Create the Base Injector
    val initInjector = createInitialInjector(config)
    val registry = initInjector.direct.instance<CommandRegistry>()

    //Launch check thread
    launchRedisCheckThread(initInjector.direct.instance())

    // Compute Command Initialization (Step 1)
    // Requires initInjector; Returns otherCommands
    val otherCommands = submitTask("InitCommands (Phase 1)") {
        val (commands, commandProviders) = scanTask()
        val (noInit, toInit) = commands
            .partition { it.isAnnotationPresent(classOf<UseFullInjector>()) }
            .let { (a, b) -> a.toSet() to b.toSet() }

        val (noInitProviders, toInitProviders) = commandProviders
            .partition { it.isAnnotationPresent(classOf<UseFullInjector>()) }
            .let { (a, b) -> a.toSet() to b.toSet() }

        initCommands(initInjector.direct, registry, toInit)
        initProviders(initInjector.direct, registry, toInitProviders)
        createPlaceholderCommands(registry, noInit)
        (noInit to noInitProviders)
    }

    //Create Shard Manager
    createShardManager(initInjector, config.botToken) { shardManager ->
        val injector = createFullInjector(initInjector, shardManager)

        // Compute Command Initialization (Step 2)
        // Requires injector, otherCommands
        queueTask("InitCommands (Phase 2)") {
            val (toInit, toInitProviders) = otherCommands()
            replacePlaceholderCommands(injector.direct, registry, toInit).forEach { queue(BUNK, it) }
            initProviders(initInjector.direct, registry, toInitProviders)
            executePostLoad(registry)
            log.info { "${registry.lookup.size} commands loaded!" }
        }

        queueTask("BotInit") {
            completeShardManager(shardManager, injector)
            log.info("ShardManager initialized!")
        }

        queue(type = BUNK) {
            injector.direct.instance<DBLPoster>().postStats()
            injector.direct.instance<DBotsPoster>().postStats()
        }
    }
}
