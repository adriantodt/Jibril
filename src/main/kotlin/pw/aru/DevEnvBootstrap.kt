package pw.aru

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import pw.aru.main as bootstrapMain

object DevEnvBootstrap {
    @JvmStatic
    fun main(args: Array<String>) {
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

        }

        Thread(devenv, ::startDevEnv, "DevEnv-Main").start()

        startupLock.join()

        bootstrapMain()
    }
}
