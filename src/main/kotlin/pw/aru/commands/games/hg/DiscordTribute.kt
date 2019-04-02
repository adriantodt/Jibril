package pw.aru.commands.games.hg

import com.mewna.catnip.entity.guild.Member
import pw.aru.hungergames.game.Tribute

class DiscordTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName()

    override fun copy() = DiscordTribute(member)
}

class DiscordGuestTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName()

    override fun copy() = DiscordGuestTribute(member)
}