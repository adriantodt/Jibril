package pw.aru.commands.info

import com.mewna.catnip.Catnip
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling

@Command("about")
class About(private val catnip: Catnip) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    override fun CommandContext.call() {
        when (args) {
            "credits", "credit" -> credits()
            "aru", "me", "bot", "" -> about()
            else -> showHelp()
        }
    }

    private fun discordTag(id: String): String {
        return catnip.cache().user(id)?.discordTag() ?: "Unknown User"
    }

    private fun CommandContext.credits() {
        sendEmbed {
            styling(message).author("Aru! | Credits").applyAll()
            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")
            field(
                "Developers",
                "${discordTag("217747278071463937")}: Main Developer"
            )
            field(
                "Other",
                "\u25AB Image and Action Commands powered by https://weeb.sh/",
                "\u25AB Thanks to ${discordTag("291710319619866624")} for providing a lot of GIFs for action commands",
                "(Also for being our first patron owo)"
            )
        }
    }

    private fun CommandContext.about() {
        sendEmbed {
            styling(message).author("Aru! | About")
            color(AruColors.primary)
            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")
            description(
                "Hi, I'm **Aru**, the personal angel guardian that your server needs!",
                "I'm here to provide you with:",
                "\u25AB **Music!** Check out `$prefix${"help music"}` to get started!",
                "\u25AB Add **fun** to your server with action commands, games and more!",
                "",
                "Let's get started? Send `$prefix${"help"}` to check my command list!",
                "",
                "Questions? Check out my **[Support server!](https://support.aru.pw)**",
                "If you feel like helping a poor angel, **[be a Patreon](https://patreon.aru.pw)** and support my development! It's as cheap as $1 a month.",
                "",
                "Send `$prefix${"about credits"}` to see the credits."
            )
            footer(
                "Invite link: https://add.aru.pw/ | Requested by ${author.effectiveName()}",
                self.effectiveAvatarUrl()
            )
        }

    }

    override val helpHandler = Help(
        CommandDescription(listOf("about"), "About Command"),
        Description("Learn more about mee!"),
        Usage(
            CommandUsage("about [me/aru/bot]", "Let me introduce myself."),
            CommandUsage("about credits", "A bit about the people that make me alive.")
        )
    )
}