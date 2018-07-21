package pw.aru.core.logging

import ch.qos.logback.classic.Level.*
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.CompositeConverter
import ch.qos.logback.core.pattern.color.ANSIConstants.*
import org.apache.commons.lang3.SystemUtils.IS_OS_UNIX
import org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS_10

class LevelHighlighter : CompositeConverter<ILoggingEvent>() {

    override fun transform(event: ILoggingEvent, input: String): String {
        val color = getForegroundColorCode(event) ?: return input
        return "$ESC_START$color$ESC_END$input$resetColor"
    }

    private fun getForegroundColorCode(event: ILoggingEvent): String? {
        return if (!isSupported) null else when (event.level.toInt()) {
            ERROR_INT -> BOLD + RED_FG
            WARN_INT -> BOLD + YELLOW_FG
            DEBUG_INT -> MAGENTA_FG
            INFO_INT -> CYAN_FG
            else -> DEFAULT_FG
        }
    }

    companion object {
        private const val resetColor = "${ESC_START}0;$DEFAULT_FG$ESC_END"
        private val RUNNING_ON_INTELLIJ = System.getProperty("java.class.path").contains("idea_rt.jar")
        private val isSupported = IS_OS_UNIX || IS_OS_WINDOWS_10 || RUNNING_ON_INTELLIJ
    }
}
