package pw.aru

import io.github.classgraph.ClassGraph
import mu.KLogging
import org.kodein.di.generic.instance
import pw.aru.bootstrap.CatnipBootstrap
import pw.aru.bootstrap.CommandBootstrap
import pw.aru.bootstrap.KodeinBootstrap
import pw.aru.core.CommandRegistry
import pw.aru.core.reporting.ErrorReportHandler
import pw.aru.core.reporting.LocalPastes
import pw.aru.utils.AsyncInfoMonitor
import java.io.File
import java.util.*
import kotlin.system.exitProcess
import kotlin.jvm.JvmStatic as static

object Bootstrap : KLogging() {
    @static
    fun main(vararg args: String) {
        Locale.setDefault(Locale("en", "US"))
        File(".vertx").deleteRecursively()
        AsyncInfoMonitor()
        ErrorReportHandler.parentUrl = Aru.aru.reportsRoot
        LocalPastes.parentUrl = Aru.aru.pastesRoot

        try {
            boot()
        } catch (e: Exception) {
            logger.error("Error during load!", e)
            logger.error("Impossible to continue, aborting...")
            exitProcess(-1)
        }
    }

    private fun boot() {
        val scanResult = ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo()
            .whitelistPackages("pw.aru")
            .scan()

        val catnipBootstrap = CatnipBootstrap()

        val catnip = catnipBootstrap.create()

        val kodein = KodeinBootstrap(catnip).create()

        catnipBootstrap.configure(catnip, kodein).thenAcceptAsync {
            val commandBootstrap = CommandBootstrap(scanResult, kodein)

            commandBootstrap.createCommands()
            commandBootstrap.createProviders()
            commandBootstrap.createStandalones()

            scanResult.close()
            val registry by kodein.instance<CommandRegistry>()
            logger.info { "Loaded ${registry.commands.size} commands!" }
        }

        catnip.connect()
    }
}
