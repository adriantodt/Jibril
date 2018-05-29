@file:JvmName("Bootstrap")
@file:JvmMultifileClass

package jibril

import jibril.Jibril.splashes
import jibril.core.CommandRegistry
import jibril.core.commands.ICommand
import jibril.core.listeners.AsyncEventWaiter
import jibril.core.listeners.CommandListener
import jibril.core.listeners.EventListeners.submit
import jibril.core.listeners.GuildListener
import jibril.core.listeners.VoiceLeaveListener
import jibril.data.config.ConfigManager
import jibril.utils.TaskManager.compute
import jibril.utils.TaskManager.queue
import jibril.utils.TaskManager.task
import jibril.utils.TaskType
import jibril.utils.api.DiscordBotsPoster
import jibril.utils.extensions.invoke
import jibril.utils.extensions.random
import jibril.utils.helpers.AsyncInfoMonitor
import mu.KotlinLogging.logger
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.Game.playing
import org.kodein.di.direct
import org.kodein.di.jit.jit
import java.util.concurrent.TimeUnit

val log = logger("jibril.Bootstrap")

internal fun start() {
    // Start-up AsyncInfoMonitor
    AsyncInfoMonitor()
    log.info { "JibrilBot starting..." }

    //Compute Reflections Scan async
    val initTask = compute(type = TaskType.PRIORITY) {
        computeReflectionsScan(basePackage = "jibril")
    }

    val config = ConfigManager.config

    //Create Shard Manager and enable DiscordLogBack
    val shardManager = createShardManager(config.tokens.discord!!)
    enableDiscordLogBack(shardManager, config)

    //Splash
    arrayOf(

        "[-=-=-=-=-=- JIBRILBOT STARTED -=-=-=-=-=-]",
        "JibrilBot v${Jibril.version} (JDA v${JDAInfo.VERSION}) started.",
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
