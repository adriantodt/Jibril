package jibril.commands.utils

import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Command("poll")
class Poll : ICommand, ICommand.Discrete {
    override val category: Category = Categories.UTILS

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val emotes = arrayOf(
        "1\u20E3", "2\u20E3", "3\u20E3", "4\u20E3", "5\u20E3", "6\u20E3", "7\u20E3", "8\u20E3", "9\u20E3", "\uD83D\uDD1F",
        "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF",
        "\u274C"
    )

    override fun discreteCall(event: GuildMessageReceivedEvent, args: String, outer: String) {
        println("Ué")

        outer.split('\n')
            .mapNotNull { it.trimStart().let { s -> emotes.firstOrNull { s.trimStart().startsWith(it) } } }
            .distinct()
            .forEach { event.message.addReaction(it).queue() }
    }
}