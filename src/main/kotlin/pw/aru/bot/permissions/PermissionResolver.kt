package pw.aru.bot.permissions

import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.user.User
import com.mewna.catnip.entity.util.Permission.*
import pw.aru.Aru.Bot.devs
import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.entities.user.UserSettings
import pw.aru.bot.patreon.Patreon
import pw.aru.core.permissions.MemberPermissions
import pw.aru.core.permissions.MemberPermissions.*
import pw.aru.core.permissions.Permission
import pw.aru.core.permissions.UserPermissions
import pw.aru.core.permissions.UserPermissions.*

class PermissionResolver(private val db: AruDB) {
    fun resolveUser(user: User): Set<UserPermissions> {
        val list = LinkedHashSet<UserPermissions>()

        val settings = UserSettings(db, user.idAsLong())

        if (!settings.blacklisted) {
            list += USE_BOT
        }

        if (Patreon.isUserPremium(db, user)) {
            list += PREMIUM
        }

        if (devs.contains(user.id())) {
            list += BOT_DEVELOPER
        }

        return list
    }

    fun resolveMember(member: Member): Set<MemberPermissions> {
        val list = LinkedHashSet<MemberPermissions>()


        if (member.idAsLong() == member.guild().ownerIdAsLong()) {
            list += OWNER
        }

        member.permissions().asSequence()
            .mapNotNull { permRemap[it] }
            .forEach(list::plusAssign)

        if (member.roles().any { adminRoles.contains(it.name().toLowerCase()) }) {
            list += ADMIN
        }

        if (member.roles().any { djRoles.contains(it.name().toLowerCase()) } || list.contains(ADMIN)) {
            list += DJ
        }

        return list
    }

    fun resolve(member: Member): Set<Permission> {
        return resolveUser(member.user()) + resolveMember(member)
    }

    private val djRoles = arrayOf("dj", "j!dj", "aru!dj")
    private val adminRoles = arrayOf("j!admin", "aru!admin")

    private val permRemap = mapOf(
        ADMINISTRATOR to ADMIN,
        MANAGE_GUILD to SERVER,
        MANAGE_ROLES to ROLES,
        MANAGE_CHANNELS to CHANNELS,
        MANAGE_MESSAGES to MESSAGES,
        MANAGE_NICKNAME to NICKNAMES,
        KICK_MEMBERS to KICK,
        BAN_MEMBERS to BAN
    )
}
