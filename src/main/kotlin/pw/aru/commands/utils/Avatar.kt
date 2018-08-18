package pw.aru.commands.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.emotes.DISAPPOINTED
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.discordTag

@Command("avatar")
class Avatar : ICommand, ICommand.HelpDialogProvider {
    override val category: Category = Category.UTILS

    override fun CommandContext.call() {
        val user = if (args.isEmpty()) {
            author
        } else {
            val list = FinderUtil.findMembers(args, guild)
            if (list.isEmpty()) {
                return send("$ERROR Aw, I couldn't find a member with that name $DISAPPOINTED").queue()
            } else if (list.size > 1) {
                return send(
                    arrayOf(
                        "$THINKING Well, I found too many users. How about refining your search?",
                        "**Users found**: ${list.joinToString(", ") { it.user.discordTag }}"
                    ).joinToString("\n")
                ).queue()
            }
            list.first()
        }.user

        send(
            "$SUCCESS Avatar for **${user.discordTag}**:\n${user.effectiveAvatarUrl}"
        ).queue()
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