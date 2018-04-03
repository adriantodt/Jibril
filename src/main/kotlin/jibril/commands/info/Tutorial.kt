package jibril.commands.info

import com.jagrosh.jdautilities.menu.MessagePaginator
import com.jagrosh.jdautilities.menu.MessagePaginator.LEFT
import com.jagrosh.jdautilities.menu.MessagePaginator.RIGHT
import jibril.Jibril
import jibril.core.CommandRegistry
import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.commands.HelpFactory
import jibril.utils.extensions.send
import jibril.utils.extensions.toSmartString
import jibril.utils.extensions.withPrefix
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Command("tutorial")
class Tutorial : ICommand, ICommand.HelpHandler {
    override val category: Category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: String) = tutorial(event, (args.toIntOrNull() ?: 1).coerceIn(1, pages.size))

    override fun onHelp(event: GuildMessageReceivedEvent) = tutorial(event, 1)

    fun tutorial(event: GuildMessageReceivedEvent, page: Int) {
        val help = pages[page]

        if (pages.isEmpty()) {
            help.onHelp(event).send(event).queue()
            return
        }

        with(MessagePaginator.Builder()) {
            setEventWaiter(Jibril.eventWaiter)
            allowTextInput(true)

            setItems(pages.size)
            setLeftRightText("previous", "next")
            setEmbedFunction { p, _ -> pages[p - 1].onHelp(event) }

            build()
        }.paginate(event.channel, page)
    }

    private val pages = listOf(
        HelpFactory("Jibril Tutorial - Page 1") {
            categoryMode()

            description(
                "Hi, I'm **Jibril**, the personal angel guardian that your server need!",
                "",
                "I'm here to provide you with:",
                "\u25AB **Music!** (Page 2)",
                "\u25AB Add **fun** to your server with fun commands (Page 3), games (Page 4) and more!",
                "\u25AB Easy moderation commands! (Page 5)",
                "\u25AB Other utilities commands! (Page 6)",
                "",
                "You can invite me to another servers with ${"invite".withPrefix()}!",
                "",
                "So, let's start!"
            )

            tutorial(
                "Use the $LEFT and $RIGHT to move through the pages."
            )

            usage("help", "Lists all commands.")
            usage("help <command>", "Displays a specific command or category's help.")
            usage("stats", "Displays my stats.")
            usage("ping", "Plays Ping-Pong with Discord and finds out how much it takes to.")
            usage("invite", "Provides Useful Links like Invite and Support Server.")
        },
        HelpFactory("Jibril Music - Page 2") {
            categoryMode()

            description("**Jibril** provides first-class music for your server!\nLet's get started!")

            usage("play [youtube/yt] <search term>", "Searches for the video in Youtube and plays it.")
            usage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and plays it.")
            usage("play", "+ attachment", "Loads and plays the song from the attachment.")
            usage("play <song url>", "Loads and plays the song from the URL.")

            usageNote("(There's also the ${listOf("playnext", "forceplay", "forceplaynext").toSmartString { "`${it.withPrefix()}`" }} commands!)")
            usageSeparator()

            usage("queue", "Displays the current queue.")
            usage("nowplaying", "Displays the current track playing.")

            usageSeparator()

            usage("pause", "Pauses the music player.")
            usage("resume", "Resumes the music player.")
            usage("shuffle", "Shuffles the queue.")

            usageNote("(There's also the ${listOf("votepause", "voteresume", "voteshuffle").toSmartString { "`${it.withPrefix()}`" }} commands!)")

            val ignored = arrayOf(
                "play", "playnext", "forceplay", "forceplaynext",
                "queue", "nowplaying",
                "pause", "resume", "shuffle",
                "votepause", "voteresume", "voteshuffle"
            )
            seeAlso(
                *CommandRegistry.lookup
                    .mapNotNull { (k, v) ->
                        if (k.category?.id != "music" || k is ICommand.Invisible || ignored.contains(v[0])) null else v[0]
                    }
                    .sorted()
                    .toTypedArray()
            )
        },
        HelpFactory("Fun Commands - Page 3") {
            categoryMode()

        },
        HelpFactory("Games - Page 4") {
            categoryMode()

            usage("hungergames", "HungerGames Simulator.")
        },
        HelpFactory("Moderation - Page 5") {
            categoryMode()

        },
        HelpFactory("Utilities - Page 6") {
            categoryMode()

            usage("avatar", "Get your avatar link.")
            usage("avatar <mention/nickname/name[#discriminator]>", "Get an user's avatar link.")
        },
        HelpFactory("Jibril Tutorial - Page 7") {
            categoryMode()
        }
    )
}