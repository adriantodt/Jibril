package pw.aru.utils

import pw.aru.utils.extensions.lang.threadGroupBasedFactory
import java.lang.Thread.currentThread
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

object AruTaskExecutor {
    private val scheduler = ScheduledThreadPoolExecutor(
        minOf(Runtime.getRuntime().availableProcessors(), 4),
        threadGroupBasedFactory("AruTaskExecutor")
    )

    fun task(
        period: Long,
        unit: TimeUnit,
        initialDelay: Long = 0,
        name: String? = null,
        runnable: () -> Unit
    ): ScheduledFuture<*> {
        return scheduler.scheduleAtFixedRate(task(runnable, name), initialDelay, period, unit)
    }

    fun queue(
        name: String? = null,
        runnable: () -> Unit
    ): CompletableFuture<*> {
        return CompletableFuture.runAsync(task(runnable, name).asRunnable(), scheduler)
    }

    fun <T> compute(
        name: String? = null,
        computable: () -> T
    ): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(task(computable, name).asSupplier(), scheduler)
    }

    fun schedule(
        delay: Long,
        unit: TimeUnit,
        name: String? = null,
        runnable: () -> Unit
    ): ScheduledFuture<*> {
        return scheduler.schedule(task(runnable, name), delay, unit)
    }

    private fun <T> task(task: () -> T, name: String?): () -> T {
        return name?.let {
            {
                val t = currentThread()
                val n = t.name
                t.name = name

                try {
                    task()
                } finally {
                    t.name = n
                }
            }
        } ?: task
    }

    private fun (() -> Unit).asRunnable(): Runnable = Runnable(this::invoke)

    private fun <R> (() -> R).asSupplier(): Supplier<R> = Supplier(this::invoke)
}
