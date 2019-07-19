//package pw.aru.commands.utils
//
//import com.mewna.catnip.entity.message.Message
//import net.dv8tion.jda.bot.exceptions.PermissionException
//import pw.aru.Aru
//import pw.aru.commands.utils.Iam.iam
//import pw.aru.commands.utils.Iam.iamnot
//import pw.aru.bot.categories.Category
//import pw.aru.bot.commands.ICommand
//import pw.aru.bot.commands.context.CommandContext
//import pw.aru.db.AruDB
//import pw.aru.db.entities.guild.GuildSettings
//import pw.aru.utils.text.ERROR
//import pw.aru.utils.text.SUCCESS
//
////@Command("iam")
//class IamCmd(private val db: AruDB) : ICommand {
//    override val category = Category.UTILS
//    override fun CommandContext.call() = iam(db, message, args)
//}
//
////@Command("iamnot")
//class IamNotCmd(private val db: AruDB) : ICommand {
//    override val category = Category.UTILS
//    override fun CommandContext.call() = iamnot(db, message, args)
//}
//
//object Iam {
//    fun iam(db: AruDB, message: Message, roleName: String) {
//        val settings = GuildSettings(db, message.guild.idLong)
//        val roles = settings.assignableRoles
//        val roleId = roles[roleName]
//
//        if (roleId == null) {
//            message.channel.sendMessage("$ERROR There isn't an autorole with the name ``$roleName``!")
//            return
//        }
//
//        val role = message.guild.getRoleById(roleId)
//
//        if (role == null) {
//            message.channel.sendMessage("$ERROR S-sorry! Someone deleted that role, and I can't assign it to you.")
//
//            roles.remove(roleName)
//            return
//        }
//
//        if (message.member.roles.contains(role)) {
//            message.channel.sendMessage("$ERROR You already have this role, baka!")
//            return
//        }
//
//        try {
//            message.guild.controller.addSingleRoleToMember(message.member, role)
//                .reason("${message.member.effectiveName} issued ${Aru.prefixes[0]}iam $roleName")
//                .queue {
//                    message.channel.sendMessage("$SUCCESS ${message.member.effectiveName}, you've now have the **${role.name}** role.")
//                }
//        } catch (_: PermissionException) {
//            message.channel.sendMessage("$ERROR Sorry, I can't give you the **$roleName** role! Make sure that I have `Manage Roles` and my role is above it.")
//        }
//    }
//
//    fun iamnot(db: AruDB, message: Message, roleName: String) {
//        val settings = GuildSettings(db, message.guild.idLong)
//        val roles = settings.assignableRoles
//        val roleId = roles[roleName]
//
//        if (roleId == null) {
//            message.channel.sendMessage("$ERROR There isn't an autorole with the name ``$roleName``!")
//            return
//        }
//
//        val role = message.guild.getRoleById(roleId)
//
//        if (role == null) {
//            message.channel.sendMessage("$ERROR S-sorry! Someone deleted that role, and I can't assign it to you.")
//
//            roles.remove(roleName)
//            return
//        }
//
//        if (!message.member.roles.contains(role)) {
//            message.channel.sendMessage("$ERROR You don't have this role, baka!")
//            return
//        }
//
//        try {
//            message.guild.controller.removeSingleRoleFromMember(message.member, role)
//                .reason("${message.member.effectiveName} issued ${Aru.prefixes[0]}iamnot $roleName")
//                .queue {
//                    message.channel.sendMessage("$SUCCESS ${message.member.effectiveName}, you've lost the **${role.name}** role.")
//                }
//        } catch (_: PermissionException) {
//            message.channel.sendMessage("$ERROR Sorry, I can't take you the **$roleName** role! Make sure that I have `Manage Roles` and my role is above it.")
//        }
//    }
//}
