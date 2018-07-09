package pw.aru.commands.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Categories
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.DISAPPOINTED
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.discordTag

@Command("avatar")
class Avatar : ICommand, ICommand.HelpDialogProvider {
    override val category: Category = Categories.UTILS

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        val user = if (args.isEmpty()) {
            event.member
        } else {
            val list = FinderUtil.findMembers(args, event.guild)
            if (list.isEmpty()) {
                return event.channel.sendMessage("$ERROR Aw, I couldn't find a member with that name $DISAPPOINTED").queue()
            } else if (list.size > 1) {
                return event.channel.sendMessage(
                    arrayOf(
                        "$THINKING Well, I found too many users. How about refining your search?",
                        "**Users found**: ${list.joinToString(", ") { it.user.discordTag }}"
                    ).joinToString("\n")
                ).queue()
            }
            list.first()
        }.user

        event.channel.sendMessage(
            "$SUCCESS Avatar for **${user.discordTag}**:\n${user.effectiveAvatarUrl}"
        ).queue()
    }

    override val helpHandler = HelpFactory("Avatar Command") {
        description("Gives the avatar URL of a specific user.")

        usage("avatar", "Get your avatar link.")
        usage("avatar <mention/nickname/name[#discriminator]>", "Get an user's avatar link.")
    }
}