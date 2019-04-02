package pw.aru.commands.utils

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings
import pw.aru.utils.AruColors.primary
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.extensions.lib.footer
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.HEART
import pw.aru.utils.text.SAD

@Command("vipstatus")
class VipStatus(private val db: AruDB) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.UTILS

    override fun CommandContext.call() {
        val args = parseable()

        when (args.takeString()) {
            "" -> userStatus()
            "guild", "server" -> guildStatus()
            else -> showHelp()
        }
    }

    private fun CommandContext.userStatus() {
        when {
            UserSettings(db, author.idAsLong()).legacyPremium -> {
                sendEmbed {
                    title("Your Premium Status")
                    url("https://patreon.aru.pw/")
                    color(primary)
                    thumbnail(author.effectiveAvatarUrl())
                    field("Status: LegacyPremium", "Thanks for being one of our first patrons.")
                    footer("Thank you for supporting us! $HEART")
                }
            }
            else -> {
                send("$ERROR You aren't a premium user. $SAD\nSupport us, be a premium user and get access to exclusive benefits!")
            }
        }
    }

    private fun CommandContext.guildStatus() {
        when {
            GuildSettings(db, guild.idAsLong()).legacyPremium -> {
                sendEmbed {
                    title("${guild.name()}'s Premium Status")
                    url("https://patreon.aru.pw/")
                    color(primary)
                    thumbnail(guild.iconUrl())
                    field("Status: LegacyPremium", "Thanks for being one of our first patrons.")
                    footer("Thank you for supporting us! $HEART")
                }
            }
            else -> {
                send("$ERROR This guild isn't premium. $SAD")
            }
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("vipstatus"), "VipStatus Command"),
        Description("Checks the your Premium Status, or the Premium Status of the server."),
        Usage(
            CommandUsage("vipstatus", "Checks your own Premium Status."),
            CommandUsage("vipstatus <guild/server>", "Checks the server's Premium Status.")
        )
    )
}