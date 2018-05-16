package jibril.core.commands

import jibril.core.categories.Category
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

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