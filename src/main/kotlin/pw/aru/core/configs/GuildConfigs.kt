package pw.aru.core.configs

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.database.entities.GuildSettings
import pw.aru.utils.extensions.withPrefix

object GuildConfigs : IConfigCompound {
    override val id = "guild"
    override val name = "Guild Configs"
    override val description = "Configurable guild options."
    override val map: Map<String, IConfig>
        get() = listOf(
            Prefix
        ).map { it.id to it }.toMap()

    object Prefix : ValueConfig(
        id = "prefix",
        name = "Custom Prefix",
        description = "Custom Prefix of the Guild"
    ) {
        override fun get(event: GuildMessageReceivedEvent): Any {
            val prefix = GuildSettings(event.guild.idLong).prefix ?: return "None set."
            return "``$prefix``"
        }

        override fun set(event: GuildMessageReceivedEvent, input: String): Any {
            val settings = GuildSettings(event.guild.idLong)
            settings.prefix = input
            return "``$input``"
        }
    }

    object AutoRoles : MapConfig(
        id = "autoroles",
        name = "Auto-assignable Roles",
        description = "Create roles that can be assignable with ${"iam".withPrefix()} command."
    ) {
        override fun get(event: GuildMessageReceivedEvent): Any {
            val assignableRoles = GuildSettings(event.guild.idLong).assignableRoles
            if (assignableRoles.isEmpty()) return "None set."

            return assignableRoles
                .mapValues { event.guild.getRoleById(it.value) ?: null }
                .filterValues { it != null }
                .map { (k, v) -> "\u25AB `$k`: ${v!!.name}" }
                .joinToString("\n")
        }

        override fun put(event: GuildMessageReceivedEvent, key: String, value: String): Any {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun remove(event: GuildMessageReceivedEvent, key: String): Any {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
