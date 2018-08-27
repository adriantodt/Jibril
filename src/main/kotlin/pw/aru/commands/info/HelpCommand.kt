package pw.aru.commands.info

import mu.KLogging
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.TaskManager.task
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.extensions.*
import pw.aru.utils.helpers.CommandStatsManager
import java.util.concurrent.TimeUnit

@Command("help", "h")
class HelpCommand(private val registry: CommandRegistry) : ICommand, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Category.INFO

    private var trending = emptyList<ICommand>()

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
            trending = registry.lookup.entries
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

    override fun CommandContext.call() {
        if (args.isEmpty()) {
            botHelp()
        } else {
            findHelp(args.trim().toLowerCase())
        }
    }

    private fun CommandContext.botHelp() {
        sendEmbed {
            baseEmbed(event, "Aru! | Help")

            description(
                "Here's all my commands. I'm sure you'll find the one you need!",
                "To check the command usage, type `${prefix}help <command>`."
            )

            footer("${registry.lookup.size} commands | Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)

            val t = trending
            if (t.isNotEmpty()) {
                field(
                    "Trending:",
                    t.asSequence().filter { it !is ICommand.Permission || it.permission.test(event.member) }
                        .take(10)
                        .joinToString(prefix = "`", separator = "` `", postfix = "`") { registry.lookup[it]!![0] }
                )
            }

            Category.LIST.forEach { cat ->
                if (cat.nsfw && !channel.isNSFW) {
                    val count = registry.lookup
                        .keys
                        .count { it.category == cat && (it !is ICommand.Permission || it.permission.test(event.member)) }


                    if (count > 0) field(
                        "${cat.categoryName}:",
                        "$count hidden commands. Set the channel to **NSFW** to view them."
                    )
                } else {
                    val list = registry.lookup
                        .entries
                        .filter { (c) -> c.category == cat && (c !is ICommand.Permission || c.permission.test(event.member)) }
                        .map { it.value[0] }
                        .sorted()

                    if (list.isNotEmpty()) field(
                        "${cat.categoryName}:",
                        list.joinToString(prefix = "`", separator = "` `", postfix = "`")
                    )
                }
            }
        }.queue()
    }

    private fun CommandContext.findHelp(args: String) {
        registry.commands.ifContains(args) {
            onHelp(it, event)
            return
        }

        Category.REGISTRY.ifContains(args) { category ->
            if (category is ICommand.HelpProvider) {
                category.helpHandler.onHelp(event)
                return
            }

            if (category is ICommand.HelpDialogProvider) {
                event.channel.sendMessage(category.helpHandler.onHelp(event)).queue()
                return
            }
            if (category is ICommand.HelpHandler) {
                category.onHelp(event)
                return
            }

            if (category is ICommand.HelpDialog) {
                event.channel.sendMessage(category.onHelp(event)).queue()
                return
            }

            sendEmbed {
                baseEmbed(event, "Aru! | Help: ${category.categoryName}")

                description(
                    "Here's all the category's commands. I'm sure you'll find the one you need!",
                    "To check the command usage, type `${prefix}help <command>`."
                )

                val list = registry.lookup
                    .entries
                    .filter { (c) -> c.category == category && (c !is ICommand.Permission || c.permission.test(event.member)) }
                    .map { it.value[0] }
                    .sorted()

                field(
                    "Commands:",
                    if (list.isEmpty()) "There's only dust here." else list.joinToString(prefix = "`", separator = "` `", postfix = "`")
                )


                footer("${list.size} commands | Requested by ${event.member.effectiveName}", event.author.effectiveAvatarUrl)

            }

            return
        }

        send("$ERROR There's no command or category with that name!").queue()
    }

    override val helpHandler
        get() = Help(
            CommandDescription(listOf("help", "h"), "Help Command"),
            Description("**${jokes.random()}**"),
            Usage(
                CommandUsage("help", "Lists all commands."),
                CommandUsage("help <command>", "Displays a command's help.")
            )
        )
}