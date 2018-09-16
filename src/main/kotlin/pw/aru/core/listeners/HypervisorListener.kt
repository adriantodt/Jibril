package pw.aru.core.listeners

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.EventListener
import pw.aru.core.hypervisor.AruHypervisor
import pw.aru.utils.helpers.GuildEvent
import pw.aru.utils.helpers.GuildStatsManager

class HypervisorListener(private val manager: ShardManager, private val hypervisor: AruHypervisor) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is GuildJoinEvent -> {
                GuildStatsManager.log(GuildEvent.JOIN)
                hypervisor.onGuildJoin(manager, event.guild)
            }
            is GuildLeaveEvent -> {
                GuildStatsManager.log(GuildEvent.LEAVE)
                hypervisor.onGuildLeave(manager, event.guild)
            }
        }
    }
}