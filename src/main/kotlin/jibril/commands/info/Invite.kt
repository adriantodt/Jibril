package jibril.commands.info

import jibril.core.categories.Categories
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.utils.Colors
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.SMILEY
import jibril.utils.extensions.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Command("invite", "links", "hangout", "patreon")
class Invite : ICommand, ICommand.HelpDialogProvider {
    override val category = Categories.INFO

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        embed {
            baseEmbed(event, name = "Jibril | Invite", color = Colors.jibrilPrimary)
            thumbnail("https://i.imgur.com/ZqJsEBr.jpg")

            description(
                "Oh, hai! I see you liked me, heh? $SMILEY",
                "If you want, I can help you in another servers you might own!",
                "Also, if a bug or error appears, report it in my support server!",
                "",
                "I don't like asking for this but server costs are expensive. If you feel like helping a poor angel, check out my Patreon and be my patron, so I can keep healthly and growing!"
            )

            field("Want me around on your Server?", "https://is.gd/jibril")
            field("Need support? Join my Server!", "https://is.gd/jibrilhub")
            field("Wanna support a poor angel?", "https://patreon.com/jibrilbot")

        }.send(event).queue()
    }

    override val helpHandler = HelpFactory("Links") {
        aliases("links", "hangout", "patreon")

        description("Provides Useful Links like Invite and Support Server.")
    }
}