package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.features.LuckyUser
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.THINKING
import jibril.utils.extensions.random
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

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