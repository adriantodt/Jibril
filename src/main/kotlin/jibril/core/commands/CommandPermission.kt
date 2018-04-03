package jibril.core.commands

import jibril.Jibril
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member

enum class CommandPermission(val permName: String) {
    USER("User") {
        override fun test(member: Member) = true
    },
    DJ("DJ") {
        override fun test(member: Member): Boolean =
            member.roles.any { it.name.equals("j!dj", ignoreCase = true) }
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
        override fun test(member: Member) = Jibril.config.developers.contains(member.user.id)
    };

    abstract fun test(member: Member): Boolean

    override fun toString() = permName
}
