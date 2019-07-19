package pw.aru.bot.patreon

import com.mewna.catnip.entity.guild.Guild
import com.mewna.catnip.entity.guild.Member
import com.mewna.catnip.entity.user.User
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings

/**
 * Use this class to check for Patreon privileges.
 * This class guarantees the future-proof-ness of commands
 */
object Patreon {
    fun patreonBotGuildCheck(db: AruDB, guild: Guild): Boolean {
        return when {
            GuildSettings(db, guild.idAsLong()).legacyPremium -> true
            else -> false
        }
    }

    fun isGuildPremium(db: AruDB, guild: Guild): Boolean {
        return when {
            GuildSettings(db, guild.idAsLong()).legacyPremium -> true
            else -> false
        }
    }

    fun isUserPremium(db: AruDB, user: User): Boolean {
        return when {
            UserSettings(db, user.idAsLong()).legacyPremium -> true
            else -> false
        }
    }

    fun isPremium(db: AruDB, guild: Guild, user: User): Boolean {
        return isGuildPremium(db, guild) || isUserPremium(db, user)
    }

    fun isUserPremium(db: AruDB, member: Member) = isUserPremium(db, member.user())

    fun isPremium(db: AruDB, member: Member) = isPremium(db, member.guild(), member.user())
}