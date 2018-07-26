package pw.aru.core.commands.placeholder

import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.emotes.LOADING
import java.util.*

object PlaceholderCommand : ICommand {
    override val category: Category? = null

    override fun CommandContext.call() {
        send("$LOADING Sorry, but I'm still booting up! This command will be available in a minute or so!").queue()
    }
}

class ReRoutingPlaceholderCommand : ICommand {
    override val category: Category? = null

    val queue = LinkedList<CommandContext>()

    override fun CommandContext.call() {
        queue += this

        send("$LOADING Wait just a bit, your command will be processed very soon!").queue()
    }
}