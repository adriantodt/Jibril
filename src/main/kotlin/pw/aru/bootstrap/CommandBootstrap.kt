package pw.aru.bootstrap

import io.github.classgraph.ScanResult
import mu.KLogging
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommandProvider
import pw.aru.core.executor.Executable
import pw.aru.core.executor.RunAtStartup
import pw.aru.core.executor.RunEvery
import pw.aru.libs.kodein.jit.jitInstance
import pw.aru.utils.AruTaskExecutor.queue
import pw.aru.utils.AruTaskExecutor.task
import pw.aru.utils.extensions.lang.allOf
import pw.aru.utils.extensions.lang.classOf

class CommandBootstrap(val scanResult: ScanResult, val kodein: Kodein) {
    companion object : KLogging()

    val registry by kodein.instance<CommandRegistry>()

    fun createCommands() {
        scanResult.getClassesImplementing("pw.aru.core.commands.ICommand")
            .filter { it.hasAnnotation("pw.aru.core.commands.Command") }
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
        scanResult.getClassesImplementing("pw.aru.core.commands.ICommandProvider")
            .filter { it.hasAnnotation("pw.aru.core.commands.CommandProvider") }
            .loadClasses(ICommandProvider::class.java)
            .forEach {
                try {
                    val provider = kodein.jitInstance(it)
                    provider.provide(registry)
                    processExecutable(provider)
                } catch (e: Exception) {
                    logger.error(e) { "Error while registering $it" }
                }
            }
    }

    fun createStandalones() {
        scanResult.getClassesImplementing("pw.aru.core.executor.Executable")
            .filter {
                allOf(
                    arrayOf(
                        "pw.aru.core.executor.RunAtStartup",
                        "pw.aru.core.executor.RunEvery"
                    ).any(it::hasAnnotation),
                    arrayOf(
                        "pw.aru.core.commands.ICommand",
                        "pw.aru.core.commands.ICommandProvider"
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

    private val Any.simpleName get() = javaClass.simpleName

    private fun processExecutable(it: Any) {
        if (it is Executable) {
            when {
                it.javaClass.isAnnotationPresent(classOf<RunEvery>()) -> {
                    val meta = it.javaClass.getAnnotation(classOf<RunEvery>())
                    task(meta.amount, meta.unit, meta.initialDelay, it.simpleName + meta, it::run)
                }
                it.javaClass.isAnnotationPresent(classOf<RunAtStartup>()) -> {
                    queue("${it.simpleName}@RunAtStartup", it::run)
                    it.run()
                }
                else -> {
                    logger.warn { "Error: $it is an Executable but lacks an annotation" }
                }
            }
        }
    }
}