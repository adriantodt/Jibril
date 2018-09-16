package pw.aru.core.hypervisor

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.entities.Guild
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.ICommand

interface AruHypervisor {
    fun onRegistryInit(registry: CommandRegistry) = Unit

    fun onBotStart(shardManager: ShardManager) = Unit
    fun onBotShutdown(shardManager: ShardManager) = Unit

    fun onGuildJoin(shardManager: ShardManager, guild: Guild) = Unit
    fun onGuildLeave(shardManager: ShardManager, guild: Guild) = Unit
    fun filterCommand(names: List<String>, command: ICommand): Boolean = true
}