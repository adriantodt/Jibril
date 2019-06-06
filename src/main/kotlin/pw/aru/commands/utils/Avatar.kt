package pw.aru.commands.utils

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.libs.catnip.entityfinder.EntityFinder
import pw.aru.utils.text.DISAPPOINTED
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.SUCCESS
import pw.aru.utils.text.THINKING

@Command("avatar")
class Avatar : ICommand, ICommand.HelpDialogProvider {
    override val category: Category = Category.UTILS

    override fun CommandContext.call() {
        val user = if (args.isEmpty()) {
            message.member()!!
        } else {
            val list = EntityFinder.findMembers(args, guild)
            if (list.isEmpty()) {
                send("$ERROR Aw, I couldn't find a member with that name $DISAPPOINTED")
                return
            } else if (list.size > 1) {
                send(
                    arrayOf(
                        "$THINKING Well, I found too many users. How about refining your search?",
                        "**Users found**: ${list.joinToString(", ") { it.user().discordTag() }}"
                    ).joinToString("\n")
                )
                return
            }
            list.first()
        }.user()

        send(
            "$SUCCESS Avatar for **${user.discordTag()}**:\n${user.effectiveAvatarUrl()}"
        )
    }

    override val helpHandler = Help(
        CommandDescription(listOf("avatar"), "Avatar Command"),
        Description("Gives the avatar URL of a specific user."),
        Usage(
            CommandUsage("avatar", "Get your avatar link."),
            CommandUsage("avatar <mention/nickname/name[#discriminator]>", "Get an user's avatar link.")
        )
    )
}