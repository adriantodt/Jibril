package pw.aru.commands.info

import net.dv8tion.jda.bot.sharding.ShardManager
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.*

@Command("about")
@UseFullInjector
class About(private val shardManager: ShardManager) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    override fun CommandContext.call() {
        when (args) {
            "credits", "credit" -> credits()
            "aru", "me", "bot", "" -> about()
            else -> showHelp()
        }
    }

    private fun discordTag(id: String): String {
        val user = shardManager.getUserById(id) ?: return "Unknown User"
        return "**${user.name}#${user.discriminator}**"
    }

    private fun CommandContext.credits() {
        sendEmbed {
            baseEmbed(event, "Aru! | Credits")
            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")
            field(
                "Developers",
                "${discordTag("217747278071463937")}: Main Developer"
            )
            field(
                "Other",
                "\u25AB Image and Action Commands powered by https://weeb.sh/"
            )
        }.queue()
    }

    private fun CommandContext.about() {
        sendEmbed {
            baseEmbed(event, name = "Aru! | About", color = AruColors.primary)
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
                "Invite link: https://add.aru.pw/ | Requested by ${event.member.effectiveName}",
                event.jda.selfUser.effectiveAvatarUrl
            )
        }.queue()

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