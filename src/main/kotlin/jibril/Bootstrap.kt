@file:JvmName("Bootstrap")

package jibril

import jibril.Jibril.bootQuotes
import jibril.Jibril.splashes
import jibril.core.CommandRegistry
import jibril.core.commands.ICommand
import jibril.core.init.BotInitializer
import jibril.core.init.BotInjections
import jibril.core.listeners.AsyncEventWaiter
import jibril.core.listeners.CommandListener
import jibril.core.listeners.EventListeners.submit
import jibril.core.listeners.GuildListener
import jibril.core.listeners.VoiceLeaveListener
import jibril.data.config.ConfigManager
import jibril.logging.DiscordLogBack
import jibril.logging.Level
import jibril.logging.LogHookManager
import jibril.logging.TerminalConsoleAdaptor
import jibril.utils.TaskManager.compute
import jibril.utils.TaskManager.task
import jibril.utils.TaskType
import jibril.utils.api.DBLPoster
import jibril.utils.api.JAPIPoster
import jibril.utils.extensions.*
import jibril.utils.helpers.AsyncInfoMonitor
import mu.KotlinLogging.logger
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.Game.playing
import net.dv8tion.jda.core.requests.RestAction
import java.util.*
import java.util.concurrent.TimeUnit

val log = logger {}

fun start() {
    AsyncInfoMonitor.init()
    log.info { "JibrilBot starting..." }

    val initializer = compute(type = TaskType.PRIORITY) {
        BotInitializer("jibril")
    }

    val config = Jibril.config

    val shardManager = shardManager {
        setToken(config.tokens.discord)
        setAutoReconnect(true)
        setAudioEnabled(true)
        setCorePoolSize(5)
        setGame(playing(bootQuotes.random()))
    }

    DiscordLogBack.enable(
        shardManager
            .getShardForGuild(config.channels.guild!!)
            .blockUntil(JDA.Status.CONNECTED)
            .getTextChannelById(config.channels.logging!!)
    )

    log.info { "[-=-=-=-=-=- JIBRILBOT STARTED -=-=-=-=-=-]" }
    log.info { "JibrilBot v${Jibril.version} (JDA v${JDAInfo.VERSION}) started." }
    log.info { "[-=-=-=-=-=- -=-=-=-=-=-=-=-=- -=-=-=-=-=-]" }

    val selfUser = shardManager.shards[0].selfUser
    val injector = BotInjections(shardManager, LogHookManager(Level.ALL, selfUser.name, selfUser.effectiveAvatarUrl)).toInjector()

    initializer().initialize(injector)

    shardManager.addEventListener(
        CommandListener,
        AsyncEventWaiter,
        injector<VoiceLeaveListener>(),
        injector<GuildListener>()
    )

    submit("Bootstrap:StatsPoster") {
        injector<DBLPoster>().postStats()
    }

    task(1, TimeUnit.MINUTES) {
        shardManager.shards.forEach { jda ->
            jda.presence.game = playing("${config.prefixes[0]}help | ${splashes.random()} [${jda.shardInfo.shardId}]")
        }
    }

    if (config.api.enabled) {
        val statsPoster = injector<JAPIPoster>()
        task(1, TimeUnit.MINUTES) {
            statsPoster.postStats()
        }
    }

    CommandRegistry.lookup.keys.forEach {
        if (it is ICommand.PostLoad) submit("PostLoad:${it.javaClass.simpleName}") { it.postLoad() }
    }

    log.info { "Finished! ${CommandRegistry.lookup.size} commands loaded!" }

    ConfigManager.save()
}

fun main(args: Array<String>) {
    Locale.setDefault(Locale("en", "US"))

    TerminalConsoleAdaptor.initializeTerminal()

    try {
        start()
    } catch (e: Exception) {
        RestAction.DEFAULT_FAILURE
        DiscordLogBack.disable()
        log.error("Error during load!", e)
        log.error("Impossible to continue, aborting...")
        System.exit(-1)
    }
}
