package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import mu.KLogging
import pw.aru.bot.CommandRegistry
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunAtStartup
import pw.aru.commands.actions.impl.ImageBasedCommandImpl
import pw.aru.commands.actions.impl.providers.WeebProvider
import pw.aru.utils.extensions.lang.getValue

@RunAtStartup
class WeebVerifier(val registry: CommandRegistry, val weebSh: Weeb4J) : Executable {
    override fun run() {
        val allTypes by weebSh.imageProvider.imageTypes.submit()
        val usedTypes = registry.lookup.keys.asSequence()
            .mapNotNull { it as? ImageBasedCommandImpl }
            .flatMap { c -> sequenceOf(c.provider, c.nsfwProvider) }
            .mapNotNull { (it as? WeebProvider)?.type }
            .toHashSet()

        val newTypes = allTypes.types.filter { it !in usedTypes }

        if (newTypes.isNotEmpty()) {
            logger.warn { "New image types available: ${newTypes.joinToString()}" }
        }
    }

    companion object : KLogging()
}