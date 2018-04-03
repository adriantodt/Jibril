package jibril.commands.funny

import jibril.core.categories.Categories
import jibril.core.commands.ArgsCommand
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.features.LuckyUser
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.THINKING
import jibril.utils.extensions.random
import jibril.utils.extensions.showHelp
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import xyz.cuteclouds.utils.args.ParserOptions
import xyz.cuteclouds.utils.args.tuples.Pair
import xyz.cuteclouds.utils.args.tuples.Text
import xyz.cuteclouds.utils.args.tuples.Tuple

@Command("choose")
class Choose : ArgsCommand(ParserOptions.NO_IMPLICIT_TUPLES), ICommand.HelpDialogProvider {
    private fun recursiveGet(tuple: Tuple): List<String> {
        return tuple.map {
            when (it) {
                is Text -> listOf(it.value())
                is Tuple -> recursiveGet(tuple)
                is Pair -> listOf(it.key + ": " + it.value)
                else -> throw IllegalStateException("Arg $it")
            }
        }.flatten()
    }

    override fun call(event: GuildMessageReceivedEvent, args: Tuple) {
        val options = recursiveGet(args)
        if (options.size < 2) showHelp()
        event.channel.sendMessage("$THINKING Hmmm... I choose `${options.random()}`!").queue(LuckyUser(event))
    }

    override val category = Categories.FUN

    override val helpHandler = HelpFactory("Choose Command") {
        description("Decisions are though, huh?")

        usage("choose <option 1>, <option 2>, [other options separated by comma...]", "Choose one of the options.")
    }
}