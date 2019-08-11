package pw.aru.bot.commands

import com.mewna.catnip.entity.message.Embed
import com.mewna.catnip.entity.message.Message
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.ICommand.CustomHandler.Result
import pw.aru.bot.commands.context.CommandContext
import pw.aru.core.permissions.Permissions

interface ICommand {
    val category: Category?

    fun CommandContext.call()

    fun nsfw(): Boolean {
        return category?.nsfw ?: false
    }

    interface Discrete : ICommand {
        fun CommandContext.discreteCall(outer: String)
    }

    interface Permission : ICommand {
        val permissions: Permissions
    }

    interface ExceptionHandler : ICommand {
        fun handle(message: Message, t: Throwable)
    }

    interface HelpDialog {
        fun onHelp(message: Message): Embed
    }

    interface HelpDialogProvider {
        val helpHandler: HelpDialog
    }

    interface CustomHandler : ICommand {
        enum class Result {
            IGNORE, HANDLED
        }

        fun CommandContext.customCall(command: String): Result
    }

    interface CustomDiscreteHandler : ICommand {
        fun CommandContext.customCall(command: String, outer: String): Result
    }
}