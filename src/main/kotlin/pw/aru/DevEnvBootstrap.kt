package pw.aru

import pw.aru.core.logging.DiscordLogBack
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture

object DevEnvBootstrap {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val devenv = ThreadGroup("devenv")
            val startupLock = CompletableFuture<Unit>()

            fun startDevEnv() {
                val redis = ProcessBuilder()
                    .directory(File("redis"))
                    .command("redis/redis-server.exe", "redis.conf")
                    .start()

                val andesite = ProcessBuilder()
                    .directory(File("andesite"))
                    .command("java", "-Xms128m", "-Xmx128m", "-jar", "andesite.jar")
                    .start()

                startupLock.complete(Unit)

                object : Thread("Redis-LogThread") {
                    init {
                        start()
                    }

                    override fun run() {
                        val reader = BufferedReader(InputStreamReader(redis.inputStream))
                        while (!interrupted()) {
                            try {
                                println(reader.readLine() ?: return)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }

                }

                object : Thread("Andesite-LogThread") {
                    init {
                        start()
                    }

                    override fun run() {
                        val reader = BufferedReader(InputStreamReader(andesite.inputStream))
                        while (!interrupted()) {
                            try {
                                println(reader.readLine() ?: return)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }

                Runtime.getRuntime().addShutdownHook(
                    object : Thread("Kill-DevEnd") {
                        override fun run() {
                            redis.destroyForcibly()
                            andesite.destroyForcibly()
                        }
                    }
                )
            }

            Thread(devenv, ::startDevEnv, "DevEnv-Main").start()

            startupLock.join()

            Bootstrap.dev = true
            Bootstrap.main()
        } catch (e: Exception) {
            DiscordLogBack.disable()
            Bootstrap.logger.error("Error during load!", e)
            Bootstrap.logger.error("Impossible to continue, aborting...")
            System.exit(-1)
        }
    }
}
