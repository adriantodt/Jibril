package pw.aru.utils

import pw.aru.utils.extensions.lang.anyOf

object OSUtils {
    val osName by lazy { runCatching { System.getProperty("os.name") }.getOrNull() }

    fun isOS(name: String): Boolean {
        return osName?.startsWith(name) ?: false
    }

    val isUnix by lazy {
        osName?.run {
            anyOf(
                startsWith("AIX"),
                startsWith("HP-UX"),
                startsWith("Irix"),
                startsWith("Linux"),
                startsWith("LINUX"),
                startsWith("FreeBSD"),
                startsWith("OpenBSD"),
                startsWith("NetBSD"),
                startsWith("Solaris"),
                startsWith("SunOS"),
                startsWith("Mac OS X")
            )
        } ?: false
    }

    val isWindows by lazy {
        osName?.startsWith("Windows") ?: false
    }
}
