package pw.aru.commands.info

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.utils.AruColors
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.SMILEY
import pw.aru.utils.extensions.*

@Command("invite", "links", "hangout", "patreon")
class Invite : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        embed {
            baseEmbed(event, name = "Aru | Invite", color = AruColors.primary)

            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")

            description(
                "Oh, hai! I see you liked me, heh? $SMILEY",
                "If you want, I can help you in another servers you might own!",
                "Also, if a bug or error appears, report it in my support server!",
                "",
                "I don't like asking for this but server costs are expensive. If you feel like helping a poor angel, check out my Patreon and be my patron, so I can keep healthly and growing!"
            )

            field("Want me around on your Server?", "https://add.aru.pw/")
            field("Need support? Join my Server!", "https://support.aru.pw/")
            field("Wanna support a poor angel?", "https://patreon.aru.pw/")

        }.send(event).queue()
    }

    override val helpHandler = HelpFactory("Invite and other Links") {
        aliases("links", "hangout", "patreon")

        description("Provides Useful Links like Invite and Support Server.")
    }
}