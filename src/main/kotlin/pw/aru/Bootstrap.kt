package pw.aru

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import com.github.samophis.lavaclient.entities.LavaClient
import com.github.samophis.lavaclient.entities.LavaClientOptions
import com.mewna.catnip.Catnip
import com.mewna.catnip.CatnipOptions
import com.mewna.catnip.entity.user.Presence
import com.mewna.catnip.shard.DiscordEvent
import gg.amy.catnip.utilities.waiter.EventExtension
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.dns.AddressResolverOptions
import mu.KLogging
import okhttp3.OkHttpClient
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import pw.aru.Aru.Bot.myAru
import pw.aru.Aru.Bot.splashes
import pw.aru.commands.games.manager.GameManager
import pw.aru.core.CommandProcessor
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommandProvider
import pw.aru.core.config.AruConfig
import pw.aru.core.executor.Executable
import pw.aru.core.listeners.CommandListener
import pw.aru.core.logging.DiscordLogBack
import pw.aru.core.music.MusicSystem
import pw.aru.core.permissions.PermissionResolver
import pw.aru.core.reporting.ErrorReportHandler
import pw.aru.core.reporting.LocalPastes
import pw.aru.db.AruDB
import pw.aru.exported.aru_version
import pw.aru.io.AruIO
import pw.aru.kodein.jit.installJit
import pw.aru.kodein.jit.jitInstance
import pw.aru.utils.*
import pw.aru.utils.Properties
import pw.aru.utils.extensions.lang.classOf
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class Bootstrap {
    companion object : KLogging()

    enum class Mode { SYNC, ASYNC }

    fun boot() {
        val scanResult = doScanResult()
        val config = loadConfig()

        val catnip = makeCatnipAsync(config).join()
        val aru = detectAru(config)

        startDiscordLogBack(catnip, config)

        val kodein = makeInjector(aru, config, catnip)

        configureCatnip(catnip, kodein).thenAccept {
            createCommands(scanResult, kodein)
        }

        connectCatnip(catnip)

    }

    private fun doScanResult(): ScanResult {
        return ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .whitelistPackages("pw.aru")
            .scan()
    }

    private fun loadConfig(): AruConfig {
        val mapper = jacksonObjectMapper()
        val file = File("aru.properties")
        val backupFile = File("aru.properties.bkp")

        try {
            return mapper.convertValue(Properties.fromFile(file))
        } catch (e: Exception) {
            if (e !is FileNotFoundException) file.renameTo(backupFile)
            Properties().apply {
                putAll(mapper.convertValue<Map<String, String>>(AruConfig()))
            }.storeToString("Aru Config")
            throw e
        }
    }

    private fun makeCatnipAsync(config: AruConfig): CompletableFuture<Catnip> {
        return Catnip.catnipAsync(
            CatnipOptions(config.botToken)
                .presence(
                    Presence.of(
                        Presence.OnlineStatus.DND,
                        Presence.Activity.of("Aru! is booting up...", Presence.ActivityType.PLAYING)
                    )
                ),
            Vertx.vertx(
                VertxOptions()
                    .setAddressResolverOptions(
                        AddressResolverOptions().addServer("8.8.8.8")
                    )
            )
        )
    }

    private fun detectAru(config: AruConfig): Aru {
        val aru = Aru.fromString(config.type)
        myAru = aru
        ErrorReportHandler.parentUrl = aru.reportsRoot
        LocalPastes.parentUrl = aru.pastesRoot
        return aru
    }

    private fun startDiscordLogBack(catnip: Catnip, config: AruConfig) {
        DiscordLogBack.enable(catnip, config.consoleWebhook)
    }

    private fun makeInjector(aru: Aru, config: AruConfig, catnip: Catnip): Kodein {
        return Kodein {
            // Install JIT Module
            installJit()

            // Self-references
            bind<Kodein>() with singleton { kodein }
            bind<DKodein>() with singleton { dkodein }

            // Instances
            bind<AruConfig>() with instance(config)
            bind<Aru>() with instance(aru)
            bind<AruDB>() with singleton { AruDB(aru.side, 0) }
            bind<AruIO>() with singleton { instance<AruDB>().io() }
            bind<CommandRegistry>() with singleton { CommandRegistry() }
            bind<CommandProcessor>() with singleton { CommandProcessor(instance(), instance(), instance()) }
            bind<ReloadableListProvider>() with singleton { ReloadableListProvider() }
            bind<PermissionResolver>() with singleton { PermissionResolver(instance()) }
            bind<Catnip>() with instance(catnip)

            bind<LavaClient>() with singleton {
                LavaClient.from(
                    LavaClientOptions()
                        .userId(catnip.selfUser()!!.idAsLong())
                        .shardCount(catnip.gatewayInfo()!!.shards())
                )
            }

            // Managers
            bind<GameManager>() with singleton { GameManager(kodein) }
            bind<MusicSystem>() with singleton { MusicSystem(instance(), instance()) }

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

    private fun createCommands(scanResult: ScanResult, kodein: Kodein) {

        val commands = scanResult.getClassesImplementing("pw.aru.core.commands.ICommand")
            .filter { it.hasAnnotation("pw.aru.core.commands.Command") }
            .loadClasses(ICommand::class.java)

        val providers = scanResult.getClassesImplementing("pw.aru.core.commands.ICommandProvider")
            .filter { it.hasAnnotation("pw.aru.core.commands.CommandProvider") }
            .loadClasses(ICommandProvider::class.java)

        val standalones = scanResult.getClassesImplementing("pw.aru.core.executor.Executable")
            .filter {
                arrayOf(
                    "pw.aru.core.commands.ICommand",
                    "pw.aru.core.commands.ICommandProvider"
                ).none(it::implementsInterface) && it.hasAnnotation("pw.aru.core.executor.RunAtStartup")
            }
            .loadClasses(Executable::class.java)

        scanResult.close()

        val registry by kodein.instance<CommandRegistry>()

        commands.forEach {
            try {
                val meta = it.getAnnotation(classOf<Command>())
                val command = kodein.jitInstance(it)

                registry.register(meta.value.toList(), command)

                if (command is Executable) command.run()
            } catch (e: Exception) {
                logger.error(e) { "Error while registering $it" }
            }
        }

        providers.forEach {
            try {
                val provider = kodein.jitInstance(it)
                provider.provide(registry)

                if (provider is Executable) provider.run()
            } catch (e: Exception) {
                logger.error(e) { "Error while registering $it" }
            }
        }

        logger.info { "Loaded ${registry.commands.size} commands!" }

        standalones.mapNotNull {
            try {
                kodein.jitInstance(it)
            } catch (e: Exception) {
                logger.error(e) { "Error while executing $it" }
                null
            }
        }.forEach { thread(name = "${it.javaClass.simpleName}@RunAtStartup", block = it::run) }
    }

    private fun configureCatnip(catnip: Catnip, kodein: Kodein): CompletableFuture<Catnip> {
        val instance by kodein.instance<CommandListener>()

        catnip
            .loadExtension(KodeinExtension(kodein))
            .loadExtension(EventExtension())

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

        allShardsReady.thenAcceptAsync {
            AruTaskExecutor.task(1, TimeUnit.MINUTES) {
                for (i in 0 until shardCount) catnip.presence(
                    Presence.of(
                        Presence.OnlineStatus.ONLINE,
                        Presence.Activity.of(
                            "${aru.prefixes[0]}help | ${splashes.random()} [$i]",
                            Presence.ActivityType.PLAYING
                        )
                    )
                )
            }

            logger.info { "${aru.botName} loaded!" }
        }

        return onAnyReady
    }

    private fun connectCatnip(catnip: Catnip) {
        catnip.connect()
    }

}

fun main() {
    AsyncInfoMonitor()
    Locale.setDefault(Locale("en", "US"))

    try {
        Bootstrap().boot()
    } catch (e: Exception) {
        DiscordLogBack.disable()
        Bootstrap.logger.error("Error during load!", e)
        Bootstrap.logger.error("Impossible to continue, aborting...")
        System.exit(-1)
    }
}