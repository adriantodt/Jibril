package pw.aru.commands.info

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.utils.AruColors
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.*

@Command("about")
@UseFullInjector
class About(private val shardManager: ShardManager) : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        when (args) {
            "credits" -> credits(event)
            "aru", "me", "bot", "" -> about(event)
            else -> showHelp()
        }
    }

    private fun discordTag(id: String): String {
        val user = shardManager.getUserById(id)
        return "**${user.name}#${user.discriminator}**"
    }

    private fun credits(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, "Aru Bot | Credits")
            field(
                "Developers",
                "${discordTag("217747278071463937")}: Main Developer",
                false
            )
        }.send(event).queue()
    }

    private fun about(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, name = "Aru | About", color = AruColors.primary)
            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")
            description(
                "Hi, I'm **Aru**, the personal angel guardian that your server need!",
                "I'm here to provide you with:",
                "\u25AB **Music!** Check out `${"help music".withPrefix()}` to get started!",
                "\u25AB Add **fun** to your server with action commands, games and more!",
                "",
                "Wanna get started? Send `${"help".withPrefix()}` to check my command list!",
                "",
                "Questions? Check out my **[Support server!](https://discord.gg/WDyhg9F)**",
                "If you feel like helping a poor angel, **[be a Patreon](https://patreon.com/arubot)** and support my development!"
            )
            footer(
                "Invite link: https://add.aru.pw/ | Requested by ${event.member.effectiveName}",
                event.jda.selfUser.effectiveAvatarUrl
            )
        }.send(event).queue()

    }

    override val helpHandler = HelpFactory("About Command") {
        description("Learn more about mee!")

        usage("about [me/aru/bot]", "Let me introduce myself.")
        usage("about credits", "A bit about the people that make me alive.")
    }
}