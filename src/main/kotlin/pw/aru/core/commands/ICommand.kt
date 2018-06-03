package pw.aru.core.commands

import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category

interface ICommand {
    val category: Category?

    fun call(event: GuildMessageReceivedEvent, args: String)

    interface Discrete : ICommand {
        fun discreteCall(event: GuildMessageReceivedEvent, args: String, outer: String)
    }

    interface Permission {
        val permission: CommandPermission
    }

    interface ExceptionHandler {
        fun handle(event: GuildMessageReceivedEvent, exception: Exception)
    }

    interface HelpDialog {
        fun onHelp(event: GuildMessageReceivedEvent): MessageEmbed
    }

    interface HelpHandler {
        fun onHelp(event: GuildMessageReceivedEvent)
    }

    interface HelpDialogProvider {
        val helpHandler: HelpDialog
    }

    interface HelpProvider {
        val helpHandler: HelpHandler
    }

    interface PostLoad {
        fun postLoad()
    }

    interface Invisible
}