package jibril.commands.games.hungergames

import net.dv8tion.jda.core.entities.Member
import xyz.cuteclouds.hunger.game.Tribute

class DiscordTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName

    override fun copy() = DiscordTribute(member)
}

class DiscordGuestTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName

    override fun copy() = DiscordGuestTribute(member)
}