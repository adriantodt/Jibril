package pw.aru.bot.bootstrap

import io.github.classgraph.ScanResult
import mu.KLogging
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.CommandRegistry
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.ICommandProvider
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunAtStartup
import pw.aru.bot.executor.RunEvery
import pw.aru.core.logging.DiscordLogger
import pw.aru.libs.kodein.jit.jitInstance
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.AruTaskExecutor.task
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.allOf
import pw.aru.utils.extensions.lang.classOf
import pw.aru.utils.extensions.lib.field
import java.time.OffsetDateTime

class CommandBootstrap(val scanResult: ScanResult, val kodein: Kodein) {
    companion object : KLogging()

    private val registry by kodein.instance<CommandRegistry>()
    private val listener = RegistryListener()

    class RegistryListener : CommandRegistry.Listener {
        val unnamedCommands = ArrayList<String>()
        val noHelpCommands = ArrayList<String>()
        val multipleHelpCommands = ArrayList<String>()

        val clean get() = unnamedCommands.isEmpty() && noHelpCommands.isEmpty() && multipleHelpCommands.isEmpty()

        override fun unnamedCommand(command: ICommand) {
            unnamedCommands += command.toString()
        }

        override fun noHelpCommand(command: ICommand, names: List<String>) {
            noHelpCommands += command.toString()
        }

        override fun multipleHelpCommand(command: ICommand, names: List<String>) {
            multipleHelpCommands += command.toString()
        }
    }

    init {
        registry.listener = listener
    }

    fun createCommands() {
        scanResult.getClassesImplementing("pw.aru.bot.commands.ICommand")
            .filter { it.hasAnnotation("pw.aru.bot.commands.Command") }
            .loadClasses(ICommand::class.java)
            .forEach {
                try {
                    val meta = it.getAnnotation(classOf<Command>())
                    val command = kodein.jitInstance(it)
                    registry.register(meta.value.toList(), command)
                    processExecutable(command)
                } catch (e: Exception) {
                    logger.error(e) { "Error while registering $it" }
                }
            }
    }

    fun createProviders() {
        scanResult.getClassesImplementing("pw.aru.bot.commands.ICommandProvider")
            .filter { it.hasAnnotation("pw.aru.bot.commands.CommandProvider") }
            .loadClasses(ICommandProvider::class.java)
            .forEach {
                try {
                    val provider = kodein.jitInstance(it)
                    provider.provide(registry)
                    processExecutable(provider)
                } catch (e: Exception) {
                    logger.error(e) { "Error while registering commands through $it" }
                }
            }
    }

    fun createStandalones() {
        scanResult.getClassesImplementing("pw.aru.bot.executor.Executable")
            .filter {
                allOf(
                    arrayOf(
                        "pw.aru.bot.executor.RunAtStartup",
                        "pw.aru.bot.executor.RunEvery"
                    ).any(it::hasAnnotation),
                    arrayOf(
                        "pw.aru.bot.commands.ICommand",
                        "pw.aru.bot.commands.ICommandProvider"
                    ).none(it::implementsInterface)
                )
            }
            .loadClasses(Executable::class.java)
            .forEach {
                try {
                    processExecutable(kodein.jitInstance(it))
                } catch (e: Exception) {
                    logger.error(e) { "Error while executing $it" }
                }
            }
    }

    fun reportResults() {
        if (!listener.clean) {
            val log = DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK)
            log.embed {
                author("Command Registry Report")
                color(Colors.discordYellow)


                if (listener.unnamedCommands.isNotEmpty()) {
                    field(
                        "Unnamed Commands:",
                        listener.unnamedCommands.joinToString("\n- ", "- ")
                    )
                }

                if (listener.noHelpCommands.isNotEmpty()) {
                    field(
                        "Commands without a help interface:",
                        listener.noHelpCommands.joinToString("\n- ", "- ")
                    )
                }

                if (listener.multipleHelpCommands.isNotEmpty()) {
                    field(
                        "Commands with multiple help interfaces:",
                        listener.multipleHelpCommands.joinToString("\n- ", "- ")
                    )
                }

                timestamp(OffsetDateTime.now())
            }
        }
    }

    private fun processExecutable(it: Any) {
        if (it is Executable) {
            when {
                it.javaClass.isAnnotationPresent(classOf<RunEvery>()) -> {
                    val meta = it.javaClass.getAnnotation(classOf<RunEvery>())
                    task(meta.amount, meta.unit, meta.initialDelay, it.simpleName + meta, it::run)
                }
                it.javaClass.isAnnotationPresent(classOf<RunAtStartup>()) -> {
                    queue("${it.simpleName}@RunAtStartup", it::run)
                }
                else -> {
                    logger.warn { "Error: $it is an Executable but lacks an annotation" }
                }
            }
        }
    }

    private val Any.simpleName get() = javaClass.simpleName
}
