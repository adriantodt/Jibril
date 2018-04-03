package jibril.commands.info

import jibril.core.CommandRegistry.commands
import jibril.core.CommandRegistry.lookup
import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.TaskManager.task
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.ERROR
import jibril.utils.extensions.*
import jibril.utils.helpers.CommandStatsManager
import mu.KLogging
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.TimeUnit

@Command("help", "h")
class Help : ICommand, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Categories.INFO

    var trending = emptyList<ICommand>()

    init {

        fun trendValue(arr: Array<out String>): Long {
            var v = 0L
            for (it in arr) {
                v += CommandStatsManager.day[it]?.get() ?: 0
                v += CommandStatsManager.hour[it]?.get() ?: 0
                v += CommandStatsManager.minute[it]?.get() ?: 0
            }
            return v
        }


        task(1, TimeUnit.MINUTES) {
            trending = lookup.entries
                .map { it.key to trendValue(it.value) }
                .filter { it.second != 0L && it.first.category != null }
                .sortedBy { it.second }
                .map { it.first }
                .reversed()
        }
    }

    private val jokes = listOf(
        "You helped yourself.",
        "Congrats, you managed to use the help command.",
        "Yo damn I heard you like help, because you just issued the help command to get the help about the help command.",
        "Helps you to help yourself.",
        "Help Inception.",
        "A help helping helping helping help.",
        "I wonder if this is what you are looking for..."
    )

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (args.isEmpty()) {
            botHelp(event)
        } else {
            findHelp(event, args.trim().toLowerCase())
        }
    }

    private fun botHelp(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, "Jibril Bot | Help")

            description(
                "Here's all my commands. I'm sure you'll find the one you need!",
                "To check the command usage, type `j!help <command>`."
            )

            val t = trending
            if (t.isNotEmpty()) {
                field(
                    "Trending:",
                    t.asSequence().filter { it !is ICommand.Permission || it.permission.test(event.member) }
                        .take(10)
                        .joinToString(prefix = "`", separator = "` `", postfix = "`") { lookup[it]!![0] },
                    inline = false
                )
            }

            Categories.LIST.forEach { cat ->
                val list = lookup
                    .entries
                    .filter { (c) -> c.category == cat && (c !is ICommand.Permission || c.permission.test(event.member)) }
                    .map { it.value[0] }
                    .sorted()

                if (list.isNotEmpty()) field(
                    cat.name,
                    list.joinToString(prefix = "`", separator = "` `", postfix = "`"),
                    inline = false
                )
            }
        }.send(event).queue()
    }

    private fun findHelp(event: GuildMessageReceivedEvent, args: String) {
        commands.ifContains(args) {
            onHelp(it, event)
            return
        }

        Categories.REGISTRY.ifContains(args) {
            onHelp(it, event)
            return
        }

        event.channel.sendMessage("$ERROR There's no command or category with that name!").queue()
    }

    override val helpHandler: ICommand.HelpDialog
        get() = HelpFactory("Help Command") {
            aliases("h")

            description("**${jokes.random()}**")
            usage("help", "Lists all commands.")
            usage("help <command>", "Displays a command's help.")
        }
}