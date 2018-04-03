package jibril.commands.utils

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.features.LuckyUser
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.DISAPPOINTED
import jibril.utils.emotes.ERROR
import jibril.utils.emotes.SUCCESS
import jibril.utils.emotes.THINKING
import jibril.utils.extensions.discordTag
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

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
        ).queue(LuckyUser(event))
    }

    override val helpHandler = HelpFactory("Avatar Command") {
        description("Gives the avatar URL of a specific user.")

        usage("avatar", "Get your avatar link.")
        usage("avatar <mention/nickname/name[#discriminator]>", "Get an user's avatar link.")
    }
}