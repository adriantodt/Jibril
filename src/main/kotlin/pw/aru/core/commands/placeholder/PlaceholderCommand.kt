package pw.aru.core.commands.placeholder

import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.emotes.LOADING

object PlaceholderCommand : ICommand {
    override val category = null

    override fun CommandContext.call() {
        send("$LOADING Sorry, but I'm still booting up! This command will be available in a minute or so!").queue()
    }
}