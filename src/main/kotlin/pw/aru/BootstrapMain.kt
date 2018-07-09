@file:JvmName("Bootstrap")
@file:JvmMultifileClass

package pw.aru

import mu.KotlinLogging.logger
import org.kodein.di.direct
import org.kodein.di.generic.instance
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.listeners.EventListeners.queueTask
import pw.aru.core.listeners.EventListeners.submitTask
import pw.aru.data.config.ConfigManager
import pw.aru.utils.TaskManager.queue
import pw.aru.utils.TaskType
import pw.aru.utils.api.DiscordBotsPoster
import pw.aru.utils.extensions.classOf
import pw.aru.utils.extensions.invoke
import pw.aru.utils.helpers.AsyncInfoMonitor

val log = logger("pw.aru.Bootstrap")

internal fun start() {
    // Start-up AsyncInfoMonitor
    AsyncInfoMonitor()
    log.info("AruBot starting...")

    // Compute Reflections Scan async
    val scanTask = submitTask("ReflectionsScanTask") {
        computeReflectionsScan(basePackage = "pw.aru")
    }

    val config = ConfigManager.config
    Aru.prefixes += config.prefixes.split(',')

    //Create the Base Injector
    val initInjector = createInitialInjector(config)

    //Launch check thread
    launchRedisCheckThread(initInjector.direct.instance())

    // Compute Command Initialization (Step 1)
    // Requires initInjector; Returns notInitializedCommands
    val notInitializedCommands = submitTask("InitCommands (Phase 1)") {
        val commands = scanTask().commandScan
        val (noInit, toInit) = commands.partition { it.isAnnotationPresent(classOf<UseFullInjector>()) }.let { (a, b) -> a.toSet() to b.toSet() }

        initCommands(initInjector.direct, toInit)
        createPlaceholderCommands(noInit)
        noInit
    }

    //Create Shard Manager
    createShardManager(initInjector, config.botToken) {
        queueTask("DiscordLogBack StartTask") { enableDiscordLogBack(it, config) }

        val injector = createFullInjector(initInjector, it)

        // Compute Command Initialization (Step 2)
        // Requires injector, notInitializedCommands
        queueTask("InitCommands (Phase 2)") {
            replacePlaceholderCommands(injector.direct, notInitializedCommands()).forEach { queue(TaskType.BUNK, it) }
            executePostLoad()

            log.info { "${CommandRegistry.lookup.size} commands loaded!" }
        }

        queueTask("BotInit") {
            completeShardManager(it, injector)

            log.info("ShardManager initialized!")
        }

        queue(type = TaskType.BUNK) {
            injector.direct.instance<DiscordBotsPoster>().postStats()
        }
    }
}
