package pw.aru.utils.wrapper

import pw.aru.utils.extensions.lang.acquiring
import pw.aru.utils.wrapper.helper.ProcessHelper
import java.io.File
import java.net.URL
import java.util.UUID.randomUUID
import java.util.concurrent.Semaphore

class WkHtmlToImage {
    private companion object : ProcessHelper("./wkhtmltoimage", "wkhtmltoimage.exe", "run/wkhtmltoimage")

    private val limiter = Semaphore(5)

    data class Options(
        val crop_h: Int? = null,
        val crop_w: Int? = null,
        val crop_x: Int? = null,
        val crop_y: Int? = null,
        val height: Int? = null,
        val width: Int? = null,
        val quality: Int? = null
    )

    fun convert(html: String, ext: String, options: Options? = null): ByteArray {
        val uuid = randomUUID().toString()

        File("$uuid.html").writeText(html)

        limiter.acquiring {
            ProcessBuilder(makeCmd(options, "$uuid.html", "$uuid.$ext"))
                .directory(workDir)
                .start()
                .waitFor()
        }

        return File("$uuid.$ext").readBytes()
    }

    fun convert(url: URL, ext: String, options: Options? = null): ByteArray {
        val uuid = randomUUID().toString()

        limiter.acquiring {
            ProcessBuilder(makeCmd(options, url.toString(), "$uuid.$ext"))
                .directory(workDir)
                .start()
                .waitFor()
        }

        return File("$uuid.$ext").readBytes()
    }

    private fun makeCmd(options: Options?, input: String, output: String): List<String> {
        val cmd = ArrayList<String>()

        cmd += executable

        options?.apply {
            crop_h?.toString()?.also {
                cmd += "--crop-h"
                cmd += it
            }

            crop_w?.toString()?.also {
                cmd += "--crop-w"
                cmd += it
            }

            crop_x?.toString()?.also {
                cmd += "--crop-x"
                cmd += it
            }

            crop_y?.toString()?.also {
                cmd += "--crop-y"
                cmd += it
            }

            height?.toString()?.also {
                cmd += "--height"
                cmd += it
            }

            width?.toString()?.also {
                cmd += "--width"
                cmd += it
            }

            quality?.toString()?.also {
                cmd += "--quality"
                cmd += it
            }
        }

        cmd += input
        cmd += output

        return cmd
    }
}
