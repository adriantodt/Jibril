package pw.aru.commands.info

import mu.KLogging
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.AruTaskExecutor.task
import pw.aru.utils.CommandStatsManager
import pw.aru.utils.extensions.aru.onHelp
import pw.aru.utils.extensions.lang.ifContains
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling
import pw.aru.utils.text.ERROR
import java.util.concurrent.TimeUnit

@Command("help", "h")
class HelpCmd(private val registry: CommandRegistry) : ICommand, ICommand.HelpDialogProvider {
    companion object : KLogging()

    override val category = Category.INFO

    private var trending = emptyList<ICommand>()

    init {
        fun trendValue(arr: List<String>): Long {
            var v = 0L
            for (it in arr) {
                v += CommandStatsManager.day[it]?.get() ?: 0
                v += CommandStatsManager.hour[it]?.get() ?: 0
                v += CommandStatsManager.minute[it]?.get() ?: 0
            }
            return v
        }

        task(1, TimeUnit.MINUTES) {
            trending = registry.lookup.entries.asSequence()
                .map { it.key to trendValue(it.value) }
                .filter { it.second != 0L && it.first.category != null }
                .sortedBy { it.second }
                .map { it.first }
                .toList()
                .reversed()
        }
    }

    override fun CommandContext.call() {
        if (args.isEmpty()) {
            botHelp()
        } else {
            findHelp(args.trim().toLowerCase())
        }
    }

    private fun CommandContext.botHelp() {
        sendEmbed {
            styling(message).author("Aru! | Help").applyAll()

            description(
                "Here's all my commands. I'm sure you'll find the one you need!",
                "To check the command usage, type `${prefix}help <command>`."
            )

            footer(
                "${registry.lookup.size} commands | Requested by ${author.effectiveName()}",
                author.effectiveAvatarUrl()
            )

            val t = trending
            if (t.isNotEmpty()) {
                field(
                    "Trending:",
                    t.asSequence()
                        .filter { c -> (c !is ICommand.Permission || c.permissions.check(permissions)) && (!c.nsfw() || channel.nsfw()) }
                        .take(10)
                        .joinToString(prefix = "`", separator = "` `", postfix = "`") { registry.lookup[it]!![0] }
                )
            }

            var count = 0

            Category.LIST.forEach { category ->
                if (category.nsfw && !channel.nsfw()) {
                    count += registry.lookup
                        .count { (c) ->
                            c.category == category && (c !is ICommand.Permission || c.permissions.check(
                                permissions
                            ))
                        }

                } else {
                    val list = registry.lookup.entries.asSequence()
                        .filter { (c) ->
                            c.category == category && (c !is ICommand.Permission || c.permissions.check(
                                permissions
                            ))
                        }
                        .partition { (c) -> c.nsfw() }
                        .apply { count += first.size }
                        .second.asSequence()
                        .map { it.value[0] }
                        .sorted()
                        .toList()

                    if (list.isNotEmpty()) field(
                        "${category.categoryName}:",
                        list.joinToString(prefix = "`", separator = "` `", postfix = "`")
                    )
                }
            }

            if (count > 0) field(
                "More...",
                "$count hidden commands. Set the channel to **NSFW** to view them."
            )
        }
    }

    private fun CommandContext.findHelp(args: String) {
        registry.commands.ifContains(args) {
            onHelp(it, message)
            return
        }

        Category.SEARCH.ifContains(args) { category ->
            if (category.help != null) {
                send(category.help.onHelp(message))
            } else {
                sendEmbed {
                    styling(message).author("Aru! | Help: ${category.categoryName}").applyAll()

                    description(
                        "Here's all the category's commands. I'm sure you'll find the one you need!",
                        "To check the command usage, type `${prefix}help <command>`."
                    )

                    val list = registry.lookup.entries.asSequence()
                        .filter { (c) ->
                            c.category == category && (c !is ICommand.Permission || c.permissions.check(
                                permissions
                            ))
                        }
                        .map { it.value[0] }
                        .sorted()
                        .toList()


                    if (category.nsfw && !channel.nsfw()) {
                        val count = registry.lookup
                            .count { (c) ->
                                c.category == category && (c !is ICommand.Permission || c.permissions.check(
                                    permissions
                                ))
                            }

                        if (count > 0) description(
                            "$count hidden commands. Set the channel to **NSFW** to view them."
                        )
                    } else {
                        if (list.isNotEmpty()) field(
                            "Commands:",
                            list.joinToString(prefix = "`", separator = "` `", postfix = "`")
                        ) else description("There's only dust here.")
                    }

                    footer(
                        "${list.size} commands | Requested by ${author.effectiveName()}",
                        author.effectiveAvatarUrl()
                    )
                }
            }

            return
        }

        send("$ERROR There's no command or category with that name!")
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

    override val helpHandler
        get() = Help(
            CommandDescription(listOf("help", "h"), "Help Command"),
            Description("**${jokes.random()}**"),
            Usage(
                CommandUsage("help", "Lists all commands."),
                CommandUsage("help <command>", "Displays a command's help."),
                CommandUsage("help <category>", "Displays a category's help.")
            )
        )
}