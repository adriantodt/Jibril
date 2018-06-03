package pw.aru.commands.funny

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.features.LuckyUser
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.random

@Command("choose")
class Choose : ICommand, ICommand.HelpDialogProvider {

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        val options = args.split(',').map(String::trim).filter(String::isEmpty)

        event.channel.sendMessage("$THINKING Hmmm... I choose `${options.random()}`!").queue(LuckyUser(event))
    }

    override val category = Categories.FUN

    override val helpHandler = HelpFactory("Choose Command") {
        description("Decisions are though, huh?")

        usage("choose <option 1>, <option 2>, [other options separated by comma...]", "Choose one of the options.")
    }
}