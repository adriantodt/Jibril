package pw.aru.core.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import pw.aru.Aru

enum class CommandPermission(val permName: String) {
    USER("User") {
        override fun test(member: Member) = true
    },
    DJ("DJ") {
        override fun test(member: Member) =
            member.roles.any { it.name.equals("j!dj", ignoreCase = true) || it.name.equals("dj", ignoreCase = true) }
                || SERVER_ADMIN.test(member)
    },
    SERVER_ADMIN("Server Admin") {
        override fun test(member: Member) = member.isOwner
            || member.hasPermission(Permission.MANAGE_SERVER)
            || member.hasPermission(Permission.ADMINISTRATOR)
            || member.roles.any { it.name.equals("j!admin", ignoreCase = true) }
            || BOT_DEVELOPER.test(member)
    },
    BOT_DEVELOPER("Bot Developer") {
        override fun test(member: Member) = Aru.developers.contains(member.user.id)
    };

    abstract fun test(member: Member): Boolean

    override fun toString() = permName
}
