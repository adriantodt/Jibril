package pw.aru.utils.wrapper.helper

import pw.aru.utils.OSUtils
import java.io.File

open class ProcessHelper(
    unixExec: String,
    winExec: String,
    workDir: String
) {
    val executable: String = if (OSUtils.isWindows) winExec else unixExec
    val workDir: File = File(workDir).apply { mkdirs() }
}