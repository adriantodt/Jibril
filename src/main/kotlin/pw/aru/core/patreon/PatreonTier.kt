package pw.aru.core.patreon

import net.dv8tion.jda.core.entities.User
import pw.aru.db.AruDB
import pw.aru.db.entities.user.UserPledge
import pw.aru.db.entities.user.UserSettings

enum class PatreonTier(val permName: String) {
    NOT_PATRON("Not a Patreon") {
        override fun test(db: AruDB, user: User): Boolean {
            if (UserSettings(db, user.idLong).legacyPremium) return false
            if (UserPledge(db, user.idLong).enabled) return false

            return true
        }
    },
    EVERYONE("Everyone") {
        override fun test(db: AruDB, user: User): Boolean = true
    },
    ANY("Any Patreon") {
        override fun test(db: AruDB, user: User): Boolean {
            if (UserSettings(db, user.idLong).legacyPremium) return true
            if (UserPledge(db, user.idLong).enabled) return true

            return false
        }
    },
    PATREON_BOT("Patreon Bot") {
        override fun test(db: AruDB, user: User): Boolean {
            if (UserSettings(db, user.idLong).legacyPremium) return true
            if (UserPledge(db, user.idLong).run { enabled && patronBot }) return true

            return false
        }
    };

    abstract fun test(db: AruDB, user: User): Boolean

    override fun toString() = permName
}
