package pw.aru.bootstrap

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import com.mewna.catnip.Catnip
import okhttp3.OkHttpClient
import org.kodein.di.DKodein
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import pw.aru.Aru
import pw.aru.bot.CommandProcessor
import pw.aru.bot.CommandRegistry
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.permissions.PermissionResolver
import pw.aru.commands.games.manager.GameManager
import pw.aru.db.AruDB
import pw.aru.exported.aru_version
import pw.aru.io.AruIO
import pw.aru.libs.andeclient.entities.AndeClient
import pw.aru.libs.kodein.jit.installJit
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache
import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import java.io.File
import java.net.http.HttpClient
import java.util.concurrent.Executors

class KodeinBootstrap(val catnip: Catnip) {
    fun create() =  Kodein {
        // Install JIT Module
        installJit()

        // Self-references
        bind<Kodein>() with singleton { kodein }
        bind<DKodein>() with singleton { dkodein }

        // Instances
        bind<Aru>() with instance(Aru.aru)
        bind<AruDB>() with singleton {
            AruDB(Aru.aru.side, 0, "redis://${Aru.EnvVars.REDIS_HOSTNAME}:6379")
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
                .setBotInfo(Aru.aru.botName, aru_version, Aru.aru.environment)
                .build()
        }

        bind<URLCache>() with singleton { URLCache(instance(), File("cache/dwnl_imgs")) }
    }
}
