package pw.aru.utils.wrapper

import pw.aru.utils.extensions.lang.acquiring
import pw.aru.utils.wrapper.helper.ProcessHelper
import java.io.File
import java.net.URL
import java.util.UUID.randomUUID
import java.util.concurrent.Semaphore

class GraphViz {
    private companion object : ProcessHelper("./dot", "dot.exe", "run/graphviz")

    private val limiter = Semaphore(5)

    enum class Renderer {
        DOT,
        NEATO,
        TWOPI,
        CIRCO,
        FDP,
        SFDP,
        PATCHWORK,
        OSAGE
    }

    data class Options(
        val renderer: Renderer? = null,
        val size: Pair<Int, Int>? = null,
        val dpi: Int? = null
    )

    fun render(dot: String, ext: String, options: Options? = null): ByteArray {
        val uuid = randomUUID().toString()

        File("$uuid.gv").writeText(dot)

        limiter.acquiring {
            ProcessBuilder(makeCmd(options, "$uuid.gv", ext))
                .directory(workDir)
                .start()
                .waitFor()
        }

        return File("$uuid.gv.$ext").readBytes()
    }

    fun render(url: URL, ext: String, options: Options? = null): ByteArray {
        val uuid = randomUUID().toString()

        limiter.acquiring {
            ProcessBuilder(makeCmd(options, url.toString(), ext))
                .directory(workDir)
                .start()
                .waitFor()
        }

        return File("$uuid.gv.$ext").readBytes()
    }

    private fun makeCmd(options: Options?, input: String, ext: String): List<String> {
        val cmd = ArrayList<String>()

        cmd += executable

        options?.apply {
            renderer?.also {
                cmd += "-K${it.toString().toLowerCase()}"
            }

            size?.also { (h, w) ->
                cmd += "-Gsize=$h,$w!"
            }

            dpi?.also {
                cmd += "-Gdpi=$it"
            }
        }

        cmd += "-T$ext"
        cmd += "-O"
        cmd += input

        return cmd
    }
}
