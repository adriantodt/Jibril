package pw.aru.commands.moderation

import net.dv8tion.jda.core.Permission.MESSAGE_HISTORY
import net.dv8tion.jda.core.Permission.MESSAGE_MANAGE
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.commands.ArgsCommand
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.parser.Args
import pw.aru.utils.extensions.requirePerms
import pw.aru.utils.extensions.showHelp

class Prune : ArgsCommand(), ICommand.Permission {
    override val category = Categories.MODERATION
    override val permission = CommandPermission.SERVER_ADMIN

    override fun call(event: GuildMessageReceivedEvent, args: Args) {
        if (!requirePerms(event, MESSAGE_MANAGE, MESSAGE_HISTORY)) return

        val arg = args.takeString()
        when (arg) {
            "" -> showHelp()
            "member" -> pruneMembers(event, args)
            "bot" -> pruneBot(event, args)
            else -> {
                val i = arg.toIntOrNull()
                if (i != null) {
                    pruneAmount(event, i, args)
                }
            }
        }
    }

    private fun pruneAmount(event: GuildMessageReceivedEvent, amount: Int, args: Args) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun pruneBot(event: GuildMessageReceivedEvent, args: Args) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun pruneMembers(event: GuildMessageReceivedEvent, args: Args) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}