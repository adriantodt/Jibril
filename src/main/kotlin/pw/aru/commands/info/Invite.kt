package pw.aru.commands.info

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.Description
import pw.aru.bot.commands.help.Help
import pw.aru.utils.AruColors
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling
import pw.aru.utils.text.SMILE2

@Command("invite", "links", "hangout")
class Invite : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.INFO

    override fun CommandContext.call() {
        sendEmbed {
            styling(message)
                .author("Aru! | Invite")
                .autoFooter()
            color(AruColors.primary)

            thumbnail("https://assets.aru.pw/img/aru_avatar.jpg")

            description(
                "Oh, hai! I see you liked me, heh? $SMILE2",
                "If you want, I can help you in another servers you might own!",
                "Also, if a bug or error appears, report it in my support server!",
                "",
                "I don't like asking for this but server costs are expensive. If you feel like helping a poor angel, check out my Patreon and be my patron, so I can stay up and growing! It's as cheap as $1 a month."
            )

            field("I have my own website now!", "https://aru.pw/")
            field("Want me around on your Server?", "https://add.aru.pw/")
            field("Need support? Join my Server!", "https://support.aru.pw/")
            field("Wanna support a poor angel?", "https://donate.aru.pw/")
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("invite", "links", "hangout"), "Invite and other Links"),
        Description("Provides Useful Links like Invite and Support Server.")
    )
}