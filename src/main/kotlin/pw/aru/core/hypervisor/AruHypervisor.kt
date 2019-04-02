package pw.aru.core.hypervisor

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.guild.Guild
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.ICommand

interface AruHypervisor {
    fun onRegistryInit(registry: CommandRegistry) = Unit

    fun onBotStart(catnip: Catnip) = Unit
    fun onBotShutdown(catnip: Catnip) = Unit

    fun filterCommand(names: List<String>, command: ICommand): Boolean = true

    fun onGuildJoin(catnip: Catnip, guild: Guild) = Unit
    fun onGuildLeave(catnip: Catnip, guild: Guild) = Unit
}