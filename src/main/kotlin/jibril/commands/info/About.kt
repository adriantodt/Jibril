package jibril.commands.info

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.Colors
import jibril.utils.commands.HelpFactory
import jibril.utils.extensions.*
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.inject.Inject

@Command("about")
class About
@Inject constructor(
    private val shardManager: ShardManager
) : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        when (args) {
            "credits" -> credits(event)
            "jibril", "me", "bot", "" -> about(event)
            else -> showHelp()
        }
    }

    private fun discordTag(id: String): String {
        val user = shardManager.getUserById(id)
        return "**${user.name}#${user.discriminator}**"
    }

    private fun credits(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, "Jibril Bot | Credits")
            field(
                "Developers",
                "${discordTag("217747278071463937")}: Main Developer",
                false
            )
        }.send(event).queue()
    }

    private fun about(event: GuildMessageReceivedEvent) {
        embed {
            baseEmbed(event, name = "Jibril | About", color = Colors.jibrilPrimary)
            thumbnail("https://i.imgur.com/ZqJsEBr.jpg")
            description(
                "Hi, I'm **Jibril**, the personal angel guardian that your server need!",
                "I'm here to provide you with:",
                "\u25AB **Music!** Check out `${"help music".withPrefix()}` to get started!",
                "\u25AB Add **fun** to your server with action commands, games and more!",
                "",
                "Wanna get started? Send `${"help".withPrefix()}` to check my command list!",
                "",
                "Questions? Check out my **[Support server!](https://discord.gg/WDyhg9F)**",
                "If you feel like helping a poor angel, **[be a Patreon](https://patreon.com/jibrilbot)** and support my development!"
            )
            footer(
                "Invite link: http://is.gd/jibril | Requested by ${event.member.effectiveName}",
                event.jda.selfUser.effectiveAvatarUrl
            )
        }.send(event).queue()

    }

    override val helpHandler = HelpFactory("About Command") {
        description("Learn more about mee!")

        usage("about [me/jibril/bot]", "Let me introduce myself.")
        usage("about credits", "A bit about the people that make me alive.")
    }
}