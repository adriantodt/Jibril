package pw.aru.core.commands.placeholder

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.utils.emotes.LOADING
import java.util.*

object PlaceholderCommand : ICommand {
    override val category: Category? = null

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        event.channel.sendMessage("$LOADING Sorry, but I'm still booting up! This command will be available in a minute or so!").queue()
    }
}

class ReRoutingPlaceholderCommand : ICommand {
    override val category: Category? = null

    val queue = LinkedList<Pair<GuildMessageReceivedEvent, String>>()

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        queue += event to args

        event.channel.sendMessage("$LOADING Wait just a bit, your command will be processed very soon!").queue()
    }
}