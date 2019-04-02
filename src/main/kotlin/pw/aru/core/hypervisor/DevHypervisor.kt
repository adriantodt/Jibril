package pw.aru.core.hypervisor

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import pw.aru.core.config.AruConfig
import pw.aru.core.hypervisor.common.GuildWebhookLogger

class DevHypervisor(config: AruConfig) : AruHypervisor {
    private val logger = GuildWebhookLogger(config.serversWebhook)
    override fun onGuildJoin(catnip: Catnip, guild: Guild) {
        logger.onGuildJoin(catnip, guild)
    }

    override fun onGuildLeave(catnip: Catnip, guild: Guild) {
        logger.onGuildLeave(catnip, guild)
    }
}