package jibril.commands.utils

import jibril.Jibril.config
import jibril.Jibril.db
import jibril.commands.utils.Iam.iam
import jibril.commands.utils.Iam.iamnot
import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.emotes.ERROR
import jibril.utils.emotes.SUCCESS
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.PermissionException

@Command("iam")
class IamCmd : ICommand {
    override val category = Categories.UTILS
    override fun call(event: GuildMessageReceivedEvent, args: String) = iam(event, args)
}

@Command("iamnot")
class IamNotCmd : ICommand {
    override val category = Categories.UTILS
    override fun call(event: GuildMessageReceivedEvent, args: String) = iamnot(event, args)
}

object Iam {
    fun iam(event: GuildMessageReceivedEvent, roleName: String) {
        val settings = db.guildSettings[event.guild.idLong]
        val roles = settings.assignableRoles
        val roleId = roles[roleName]

        if (roleId == null) {
            event.channel.sendMessage("$ERROR There isn't an autorole with the name ``$roleName``!").queue()
            return
        }

        val role = event.guild.getRoleById(roleId)

        if (role == null) {
            event.channel.sendMessage("$ERROR S-sorry! Someone deleted that role, and I can't assign it to you.").queue()

            roles.remove(roleName)
            settings.save(db)
            return
        }

        if (event.member.roles.contains(role)) {
            event.channel.sendMessage("$ERROR You already have this role, baka!").queue()
            return
        }

        try {
            event.guild.controller.addSingleRoleToMember(event.member, role)
                .reason("${event.member.effectiveName} issued ${config.prefixes[0]}iam $roleName")
                .queue {
                    event.channel.sendMessage("$SUCCESS ${event.member.effectiveName}, you've now have the **${role.name}** role.").queue()
                }
        } catch (_: PermissionException) {
            event.channel.sendMessage("$ERROR Sorry, I can't give you the **$roleName** role! Make sure that I have `Manage Roles` and my role is above it.").queue()
        }
    }

    fun iamnot(event: GuildMessageReceivedEvent, roleName: String) {
        val settings = jibril.Jibril.db.guildSettings[event.guild.idLong]
        val roles = settings.assignableRoles
        val roleId = roles[roleName]

        if (roleId == null) {
            event.channel.sendMessage("$ERROR There isn't an autorole with the name ``$roleName``!").queue()
            return
        }

        val role = event.guild.getRoleById(roleId)

        if (role == null) {
            event.channel.sendMessage("$ERROR S-sorry! Someone deleted that role, and I can't assign it to you.").queue()

            roles.remove(roleName)
            settings.save(db)
            return
        }

        if (!event.member.roles.contains(role)) {
            event.channel.sendMessage("$ERROR You don't have this role, baka!").queue()
            return
        }

        try {
            event.guild.controller.removeSingleRoleFromMember(event.member, role)
                .reason("${event.member.effectiveName} issued ${config.prefixes[0]}iamnot $roleName")
                .queue {
                    event.channel.sendMessage("$SUCCESS ${event.member.effectiveName}, you've lost the **${role.name}** role.").queue()
                }
        } catch (_: PermissionException) {
            event.channel.sendMessage("$ERROR Sorry, I can't take you the **$roleName** role! Make sure that I have `Manage Roles` and my role is above it.").queue()
        }
    }
}
