package jibril.core.articles

import jibril.core.commands.CommandPermission
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

interface IArticle {
    interface Permission {
        val permission: CommandPermission
    }

    interface Dialog : IArticle {
        fun helpEmbed(event: GuildMessageReceivedEvent): MessageEmbed
    }

    interface Handler : IArticle {
        fun onHelp(event: GuildMessageReceivedEvent)
    }

    interface Invisible
}