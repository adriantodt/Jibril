package pw.aru.commands.games.hg

import com.mewna.catnip.entity.guild.Member
import pw.aru.hg.engine.game.Tribute
import pw.aru.utils.extensions.discordapp.safeUserInput

class DiscordTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName().safeUserInput()

    override fun copy() = DiscordTribute(member)
}

class DiscordGuestTribute(val member: Member) : Tribute() {
    override val name: String = member.effectiveName().safeUserInput()

    override fun copy() = DiscordGuestTribute(member)
}