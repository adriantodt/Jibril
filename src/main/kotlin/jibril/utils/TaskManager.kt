package jibril.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import jibril.utils.TaskType.*
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object TaskManager {
    private val PRIORITY_SCHEDULER: ScheduledThreadPoolExecutor
    private val COMMON_SCHEDULER: ScheduledThreadPoolExecutor
    private val BUNK_SCHEDULER: ScheduledThreadPoolExecutor

    init {
        val processors = minOf(Runtime.getRuntime().availableProcessors(), 4)

        PRIORITY_SCHEDULER = ScheduledThreadPoolExecutor(
            processors,
            ThreadFactoryBuilder().setNameFormat("Priority Task Thread-%d").build()
        )
        COMMON_SCHEDULER = ScheduledThreadPoolExecutor(
            processors,
            ThreadFactoryBuilder().setNameFormat("Common Task Thread-%d").build()
        )
        BUNK_SCHEDULER = ScheduledThreadPoolExecutor(
            processors,
            ThreadFactoryBuilder().setNameFormat("Bunk Task Thread-%d").build()
        )
    }

    fun scheduler(type: TaskType) = when (type) {
        PRIORITY -> PRIORITY_SCHEDULER
        COMMON -> COMMON_SCHEDULER
        BUNK -> BUNK_SCHEDULER
    }

    fun task(period: Long, unit: TimeUnit, type: TaskType = COMMON, command: () -> Unit): ScheduledFuture<*> {
        return scheduler(type).scheduleAtFixedRate(command, 0, period, unit)
    }

    fun queue(type: TaskType = COMMON, command: () -> Unit): Future<*> {
        return scheduler(type).submit(command)
    }

    fun <T> compute(type: TaskType = COMMON, command: () -> T): Future<T> {
        return scheduler(type).submit(command)
    }

    fun schedule(delay: Long, unit: TimeUnit, type: TaskType = COMMON, command: () -> Unit): ScheduledFuture<*> {
        return scheduler(type).schedule(command, delay, unit)
    }

    fun <T> scheduleCompute(delay: Long, unit: TimeUnit, type: TaskType = COMMON, command: () -> T): ScheduledFuture<T> {
        return scheduler(type).schedule(command, delay, unit)
    }
}

enum class TaskType {
    PRIORITY, COMMON, BUNK
}
