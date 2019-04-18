package pw.aru.utils.wrapper.helper

import org.apache.commons.lang3.SystemUtils
import java.io.File

open class ProcessHelper(
    unixExec: String,
    winExec: String,
    workDir: String
) {
    val executable: String = if (SystemUtils.IS_OS_WINDOWS) winExec else unixExec
    val workDir: File = File(workDir).apply { mkdirs() }
}