package pw.aru

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import com.mewna.catnip.Catnip
import com.mewna.catnip.CatnipOptions
import com.mewna.catnip.cache.CacheFlag
import com.mewna.catnip.entity.user.Presence
import com.mewna.catnip.shard.DiscordEvent
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
import pw.aru.Aru.Bot.aru
import pw.aru.Aru.Bot.splashes
import pw.aru.Aru.EnvVars.REDIS_HOSTNAME
import pw.aru.commands.games.manager.GameManager
import pw.aru.core.CommandProcessor
import pw.aru.core.CommandRegistry
import pw.aru.core.GuildWebhookLogger
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommandProvider
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
import pw.aru.libs.andeclient.entities.AndeClient
import pw.aru.libs.kodein.jit.installJit
import pw.aru.libs.kodein.jit.jitInstance
import pw.aru.utils.*
import pw.aru.utils.extensions.lang.classOf
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import java.io.File
import java.net.http.HttpClient
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class Bootstrap {
    companion object : KLogging() {
        @JvmStatic
        fun main(vararg args: String) {
            File(".vertx").deleteRecursively()
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
    }

    fun boot() {
        val scanResult = doScanResult()

        val catnip = makeCatnipAsync().join()

        ErrorReportHandler.parentUrl = aru.reportsRoot
        LocalPastes.parentUrl = aru.pastesRoot

        startDiscordLogBack(catnip)

        val kodein = makeInjector(catnip)

        configureCatnip(catnip, kodein).thenAcceptAsync {
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

    private fun makeCatnipAsync(): CompletableFuture<Catnip> {
        return Catnip.catnipAsync(
            CatnipOptions(Aru.EnvVars.BOT_TOKEN)
                .memberChunkTimeout(300000L)
                .cacheFlags(setOf(CacheFlag.DROP_GAME_STATUSES)) // I guess we don't need it
                .presence(
                    Presence.of(
                        Presence.OnlineStatus.DND,
                        Presence.Activity.of("Aru! is booting up...", Presence.ActivityType.PLAYING)
                    )
                ),
            Vertx.vertx(
                VertxOptions()
                    .setEventLoopPoolSize(8)
                    .setAddressResolverOptions(
                        AddressResolverOptions().addServer("8.8.8.8")
                    )
            )
        )
    }

    private fun startDiscordLogBack(catnip: Catnip) {
        DiscordLogBack.enable(catnip, Aru.EnvVars.CONSOLE_WEBHOOK)
    }

    private fun makeInjector(catnip: Catnip): Kodein {
        return Kodein {
            // Install JIT Module
            installJit()

            // Self-references
            bind<Kodein>() with singleton { kodein }
            bind<DKodein>() with singleton { dkodein }

            // Instances
            bind<Aru>() with instance(aru)
            bind<AruDB>() with singleton {
                AruDB(aru.side, 0, "redis://$REDIS_HOSTNAME:6379")
            }
            bind<AruIO>() with singleton { instance<AruDB>().io() }
            bind<CommandRegistry>() with singleton { CommandRegistry() }
            bind<CommandProcessor>() with singleton { CommandProcessor(instance(), instance(), instance()) }
            bind<ReloadableListProvider>() with singleton { ReloadableListProvider() }
            bind<PermissionResolver>() with singleton { PermissionResolver(instance()) }
            bind<Catnip>() with instance(catnip)

            bind<AndeClient>() with singleton {
                AndeClient.andeClient(catnip.selfUser()!!.idAsLong())
                    .httpClient(instance())
                    .create()
            }

            // Managers
            bind<GameManager>() with singleton { GameManager(kodein) }
            bind<MusicSystem>() with singleton { MusicSystem(kodein) }

            // APIs
            bind<OkHttpClient>() with singleton { OkHttpClient() }
            bind<HttpClient>() with singleton {
                HttpClient.newBuilder()
                    .executor(Executors.newFixedThreadPool(16, threadGroupBasedFactory("HttpClient")))
                    .build()
            }

            bind<Weeb4J>() with singleton {
                Weeb4J.Builder()
                    .setToken(TokenType.WOLKE, Aru.EnvVars.WEEBSH_TOKEN)
                    .setHttpClient(instance())
                    .setBotInfo(aru.botName, aru_version, aru.environment)
                    .build()
            }

            bind<URLCache>() with singleton { URLCache(instance(), File("cache/dwnl_imgs")) }
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

        catnip.loadExtension(KodeinExtension(kodein))

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

        val guildLogger = GuildWebhookLogger(Aru.EnvVars.SERVERS_WEBHOOK)
        catnip.on(DiscordEvent.GUILD_CREATE, guildLogger::onGuildJoin)
        catnip.on(DiscordEvent.GUILD_DELETE, guildLogger::onGuildLeave)

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
