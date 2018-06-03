@file:JvmName("Bootstrap")
@file:JvmMultifileClass

package pw.aru

import mu.KotlinLogging.logger
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.Game.playing
import org.kodein.di.direct
import org.kodein.di.jit.jit
import pw.aru.Aru.splashes
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.ICommand
import pw.aru.core.listeners.AsyncEventWaiter
import pw.aru.core.listeners.CommandListener
import pw.aru.core.listeners.EventListeners.submit
import pw.aru.core.listeners.GuildListener
import pw.aru.core.listeners.VoiceLeaveListener
import pw.aru.data.config.ConfigManager
import pw.aru.utils.TaskManager.compute
import pw.aru.utils.TaskManager.queue
import pw.aru.utils.TaskManager.task
import pw.aru.utils.TaskType
import pw.aru.utils.api.DiscordBotsPoster
import pw.aru.utils.extensions.invoke
import pw.aru.utils.extensions.random
import pw.aru.utils.helpers.AsyncInfoMonitor
import java.util.concurrent.TimeUnit

val log = logger("pw.aru.Bootstrap")

internal fun start() {
    // Start-up AsyncInfoMonitor
    AsyncInfoMonitor()
    log.info { "AruBot starting..." }

    //Compute Reflections Scan async
    val initTask = compute(type = TaskType.PRIORITY) {
        computeReflectionsScan(basePackage = "pw.aru")
    }

    val config = ConfigManager.config

    //Create Shard Manager and enable DiscordLogBack
    val shardManager = createShardManager(config.tokens.discord!!)
    enableDiscordLogBack(shardManager, config)

    //Splash
    arrayOf(

        "[-=-=-=-=-=- ARUBOT STARTED -=-=-=-=-=-]",
        "AruBot v${Aru.version} (JDA v${JDAInfo.VERSION}) started.",
        "[-=-=-=-=-=- -=-=-=-=-=-=-=-=- -=-=-=-=-=-]"

    ).forEach(log::info)

    //Create Injector
    val injector = createInjector(shardManager, config)
    val directInjector = injector.direct

    //Create the Commands
    val results = initTask()
    initCommands(directInjector, results.commandScan)

    with(directInjector.jit) {
        shardManager.addEventListener(
            CommandListener,

            newInstance<AsyncEventWaiter>(),
            newInstance<VoiceLeaveListener>(),
            newInstance<GuildListener>()
        )
    }

    queue(type = TaskType.BUNK) {
        directInjector.jit.newInstance<DiscordBotsPoster>().postStats()
    }

    task(1, TimeUnit.MINUTES) {
        shardManager.shards.forEach {
            it.presence.game = playing("${config.prefixes[0]}help | ${splashes.random()} [${it.shardInfo.shardId}]")
        }
    }

    CommandRegistry.lookup.keys.forEach {
        if (it is ICommand.PostLoad) {
            submit("PostLoad:${it.javaClass.simpleName}", it::postLoad)
        }
    }

    log.info { "Finished! ${CommandRegistry.lookup.size} commands loaded!" }

    ConfigManager.save()
}
