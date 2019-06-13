package pw.aru.core.options

import pw.aru.Aru
import pw.aru.Aru.Bot.aru
import pw.aru.core.executor.Executable
import pw.aru.core.executor.RunAtStartup
import pw.aru.db.entities.guild.GuildSettings

@RunAtStartup
class BotOptions(private val options: Options) : Executable {
    override fun run() {
        options.server {

            property("prefix") {
                propertyName = "Aru's prefix"
                getter = {
                    GuildSettings(db, guild.idAsLong()).mainPrefix
                }
                setter = {
                    GuildSettings(db, guild.idAsLong()).mainPrefix = args.takeString()
                }
                clear = {
                    GuildSettings(db, guild.idAsLong()).mainPrefix = null
                }
            }

            property("patreonPrefix") {
                propertyName = "AruPatreon's prefix"
                filter = { aru == Aru.PATREON }
                getter = {
                    GuildSettings(db, guild.idAsLong()).patreonPrefix
                }
                setter = {
                    GuildSettings(db, guild.idAsLong()).patreonPrefix = args.takeString()
                }
                clear = {
                    GuildSettings(db, guild.idAsLong()).patreonPrefix = null
                }
            }

            property("devPrefix") {
                propertyName = "AruDev's prefix"
                filter = { aru == Aru.DEV }
                getter = {
                    GuildSettings(db, guild.idAsLong()).devPrefix
                }
                setter = {
                    GuildSettings(db, guild.idAsLong()).devPrefix = args.takeString()
                }
                clear = {
                    GuildSettings(db, guild.idAsLong()).devPrefix = null
                }
            }


        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}