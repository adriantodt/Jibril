package jibril.utils.helpers

import com.sun.management.OperatingSystemMXBean
import jibril.utils.TaskManager.task
import jibril.utils.TaskType
import jibril.utils.extensions.floor
import mu.KLogging
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

object AsyncInfoMonitor : KLogging() {
    var availableProcessors = Runtime.getRuntime().availableProcessors()
        private set

    var cpuUsage = 0.0
        private set

    var freeMemory = 0.0
        private set

    var maxMemory = 0.0
        private set

    var threadCount = 0
        private set

    var totalMemory = 0.0
        private set

    var vpsCpuUsage = 0.0
        private set

    var vpsFreeMemory = 0.0
        private set

    var vpsMaxMemory = 0.0
        private set

    var vpsUsedMemory = 0.0
        private set

    private var lastProcessCpuTime = 0.0

    private var lastSystemTime: Long = 0

    init {
        //Useful contants
        val mb = 1048576.0
        val gb = 1073741824.0

        val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val thread = ManagementFactory.getThreadMXBean()
        val r = Runtime.getRuntime()

        fun processCpuTime(): Double = os.processCpuTime.toDouble()
        fun processCpuUsage(): Double {
            val systemTime = System.nanoTime()
            val processCpuTime = processCpuTime()

            val cpuUsage = (processCpuTime - lastProcessCpuTime) / (systemTime - lastSystemTime).toDouble()

            lastSystemTime = systemTime
            lastProcessCpuTime = processCpuTime

            return cpuUsage / availableProcessors
        }

        lastSystemTime = System.nanoTime()
        lastProcessCpuTime = processCpuTime()

        task(1, TimeUnit.SECONDS, type = TaskType.PRIORITY) {
            threadCount = thread.threadCount
            availableProcessors = r.availableProcessors()
            freeMemory = (r.freeMemory() / mb).floor(100.0)
            maxMemory = (r.maxMemory() / mb).floor(100.0)
            totalMemory = (r.totalMemory() / mb).floor(100.0)
            cpuUsage = processCpuUsage().floor(100.0)
            vpsCpuUsage = (os.systemCpuLoad * 100).floor(100.0)
            vpsFreeMemory = (os.freePhysicalMemorySize / gb).floor(100.0)
            vpsMaxMemory = (os.totalPhysicalMemorySize / gb).floor(100.0)
            vpsUsedMemory = (vpsMaxMemory - vpsFreeMemory).floor(100.0)
        }
    }

    fun init() {
        logger.info { "AsyncInfoMonitor started!" }
    }
}