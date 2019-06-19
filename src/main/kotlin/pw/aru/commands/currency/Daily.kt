package pw.aru.commands.currency

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.db.AruDB
import pw.aru.utils.ratelimiter.RateLimiterConfigurator
import java.util.concurrent.TimeUnit

@Command("daily")
class Daily(val db: AruDB) : ICommand.RateLimited {
    override val category = Category.CURRENCY

    override val rateLimiter = RateLimiterConfigurator()
        .prefix("pw.aru.ratelimit:command.daily")
        .limit(1)
        .cooldown(24, TimeUnit.HOURS)
        .maxCooldown(24, TimeUnit.HOURS)
        .randomIncrement(false)
        .build(db)

    override fun CommandContext.call() {
        rateLimiting {

        }
    }
}