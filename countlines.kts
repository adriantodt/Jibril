import java.io.File
import java.util.concurrent.atomic.AtomicLong

fun walk(vararg files: File, onFile: (File) -> Unit) {
    for (file in files) if (file.isDirectory) walk(onFile = onFile, files = *file.listFiles()) else onFile(file)
}

val charCount = AtomicLong()
val lineCount = AtomicLong()

walk(*File(".").listFiles()) {
    when (it.extension) {
        "kt", "java" -> {
            val text = it.readText()
            charCount.addAndGet(text.length.toLong())
            lineCount.addAndGet(text.count { it == '\n' }.toLong())
        }
    }
}

println("charCount = $charCount; lineCount = $lineCount")