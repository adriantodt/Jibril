package jibril.core.commands

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import xyz.cuteclouds.utils.StringUtils

abstract class AdvancedCommand : CommandWithArgs<Map<String, String>>() {
    override fun args(event: GuildMessageReceivedEvent, args: String): Map<String, String> = StringUtils.parse(event.message.contentRaw)
}