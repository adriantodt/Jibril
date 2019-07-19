package pw.aru.utils

import com.sun.management.OperatingSystemMXBean
import mu.KLogging
import pw.aru.utils.AruTaskExecutor.task
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit.SECONDS

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
        val runtime = ManagementFactory.getRuntimeMXBean()

        fun processCpuTime(): Double = os.processCpuTime.toDouble()
        fun processCpuUsage(): Double {
            val systemTime = runtime.uptime
            val processCpuTime = os.processCpuTime.toDouble()

            val cpuUsage = Math.min(
                99.99,
                (processCpuTime - lastProcessCpuTime) / ((systemTime - lastSystemTime) * 10000.0 * availableProcessors.toDouble())
            )

            lastSystemTime = systemTime
            lastProcessCpuTime = processCpuTime

            return cpuUsage
        }

        lastSystemTime = runtime.uptime
        lastProcessCpuTime = processCpuTime()

        task(1, SECONDS) {
            threadCount = thread.threadCount
            availableProcessors = os.availableProcessors
            freeMemory = floor((r.freeMemory() / mb), 100.0)
            maxMemory = floor((r.maxMemory() / mb), 100.0)
            totalMemory =
                floor((r.totalMemory() / mb), 100.0)
            cpuUsage = floor(processCpuUsage(), 100.0)
            vpsCpuUsage =
                floor((os.systemCpuLoad * 100), 100.0)
            vpsFreeMemory =
                floor((os.freePhysicalMemorySize / gb), 100.0)
            vpsMaxMemory =
                floor((os.totalPhysicalMemorySize / gb), 100.0)
            vpsUsedMemory = floor(
                (vpsMaxMemory - vpsFreeMemory),
                100.0
            )
        }
    }

    operator fun invoke() {
        logger.info { "AsyncInfoMonitor started!" }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun floor(d: Double, factor: Double = 1.0) = Math.floor(d * factor) / factor
}