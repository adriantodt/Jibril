package pw.aru.commands.games.hg

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.prefix
import pw.aru.utils.emotes.BANG
import pw.aru.utils.emotes.TALKING

@Command("hungergames", "hg")
class HungerGamesCmd : ICommand, ICommand.HelpHandler {
    override val category = null

    override fun CommandContext.call() {
        send(
            "$TALKING Oops, this command doesn't exist anymore!\n" +
                "**HungerGames** is now accessible through **GameHub**!\n\n" +
                "Run ``${prefix}gamehub new`` to create a new game lobby.\n" +
                "Then, once all people are inside the lobby, run ``${prefix}gamehub play hg`` to start.\n\n" +
                "$BANG *This command will be removed in next versions and it's here just as a placeholder.*"
        ).queue()
    }

    override fun onHelp(event: GuildMessageReceivedEvent) = CommandContext(event, "").call()

}