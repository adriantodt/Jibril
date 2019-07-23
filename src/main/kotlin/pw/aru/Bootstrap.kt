package pw.aru

import com.mewna.catnip.entity.user.Presence.Activity
import com.mewna.catnip.entity.user.Presence.ActivityType.PLAYING
import com.mewna.catnip.entity.user.Presence.OnlineStatus.ONLINE
import com.mewna.catnip.entity.user.Presence.of
import io.github.classgraph.ClassGraph
import org.kodein.di.generic.instance
import pw.aru.Aru.Bot.splashes
import pw.aru.bot.CommandRegistry
import pw.aru.bot.bootstrap.BootstrapLogger
import pw.aru.bot.bootstrap.CatnipBootstrap
import pw.aru.bot.bootstrap.CommandBootstrap
import pw.aru.bot.bootstrap.KodeinBootstrap
import pw.aru.bot.commands.help.prefix
import pw.aru.bot.reporting.ErrorReportHandler
import pw.aru.bot.reporting.LocalPastes
import pw.aru.utils.AruTaskExecutor.task
import pw.aru.utils.AsyncInfoMonitor
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import kotlin.jvm.JvmStatic as static

@Suppress("unused")
object Bootstrap {
    @static
    fun main(vararg args: String) {
        Locale.setDefault(Locale("en", "US"))
        File(".vertx").deleteRecursively()
        AsyncInfoMonitor()
        ErrorReportHandler.parentUrl = Aru.aru.reportsRoot
        LocalPastes.parentUrl = Aru.aru.pastesRoot

        val log = BootstrapLogger()
        log.started()

        try {
            val scanResult = ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages("pw.aru")
                .scan()

            val catnipBootstrap = CatnipBootstrap()

            val catnip = catnipBootstrap.create()

            val kodein = KodeinBootstrap(catnip).create()

            catnipBootstrap.run {
                onFirstShardReady = {
                    val commandBootstrap = CommandBootstrap(scanResult, kodein)

                    commandBootstrap.createCommands()
                    commandBootstrap.createProviders()
                    commandBootstrap.createStandalones()

                    scanResult.close()
                    commandBootstrap.reportResults()
                }

                onAllShardsReady = {
                    task(1, TimeUnit.MINUTES) {
                        val text = "${prefix}help | ${splashes.random()}"
                        catnip.presence(of(ONLINE, Activity.of(text, PLAYING)))
                    }

                    val registry by kodein.instance<CommandRegistry>()
                    log.successful(it, registry.commands.size)
                }

                configure(catnip, kodein)
            }
        } catch (e: Exception) {
            log.failed(e)
            exitProcess(1)
        }
    }
}
