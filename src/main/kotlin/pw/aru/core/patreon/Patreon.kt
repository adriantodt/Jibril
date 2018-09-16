package pw.aru.core.patreon

import net.dv8tion.jda.core.entities.Guild
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings

object Patreon {
    fun patreonBotGuildCheck(db: AruDB, guild: Guild): Boolean {
        if (GuildSettings(db, guild.idLong).legacyPremium) return true
        return false
    }
}