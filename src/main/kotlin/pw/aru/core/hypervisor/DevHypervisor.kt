package pw.aru.core.hypervisor

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import pw.aru.core.config.AruConfig
import pw.aru.core.hypervisor.common.GuildWebhookLogger

class DevHypervisor(config: AruConfig) : AruHypervisor {
    private val logger = GuildWebhookLogger(config.serversWebhook)
    override fun onGuildJoin(shardManager: ShardManager, guild: Guild) {
        logger.onGuildJoin(shardManager, guild)
    }

    override fun onGuildLeave(shardManager: ShardManager, guild: Guild) {
        logger.onGuildLeave(shardManager, guild)
    }
}