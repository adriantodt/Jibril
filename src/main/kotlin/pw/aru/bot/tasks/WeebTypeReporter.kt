package pw.aru.bot.tasks

import com.github.natanbc.weeb4j.Weeb4J
import com.mewna.catnip.Catnip
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import pw.aru.Aru
import pw.aru.bot.CommandRegistry
import pw.aru.bot.executor.Executable
import pw.aru.bot.executor.RunEvery
import pw.aru.commands.actions.impl.ImageBasedCommandImpl
import pw.aru.commands.actions.impl.providers.WeebProvider
import pw.aru.core.logging.DiscordLogger
import pw.aru.utils.Colors
import pw.aru.utils.extensions.lang.getValue
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@RunEvery(0, 1, TimeUnit.HOURS)
class WeebTypeReporter(override val kodein: Kodein) : Executable, KodeinAware {
    private val catnip: Catnip by instance()
    private val registry: CommandRegistry by instance()
    private val weebSh: Weeb4J by instance()

    private val logger = DiscordLogger(Aru.EnvVars.CONSOLE_WEBHOOK)
    private val knownTypes = ConcurrentHashMap.newKeySet<String>()

    init {
        knownTypes += registry.lookup.keys.asSequence()
            .mapNotNull { it as? ImageBasedCommandImpl }
            .flatMap { c -> sequenceOf(c.provider, c.nsfwProvider) }
            .mapNotNull { (it as? WeebProvider)?.type }
    }

    override fun run() {
        val allTypes by weebSh.imageProvider.imageTypes.submit()
        val newTypes = allTypes.types.filter { it !in knownTypes }

        if (newTypes.isNotEmpty()) {
            knownTypes += newTypes

            logger.embed {
                author("Weeb.sh Report")
                color(Colors.discordPink)

                description("New image types available: ${newTypes.joinToString()}")

                timestamp(OffsetDateTime.now())
            }
        }
    }
}