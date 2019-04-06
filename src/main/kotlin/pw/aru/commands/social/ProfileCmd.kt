package pw.aru.commands.social

import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.parser.tryTakeMember
import pw.aru.db.AruDB
import pw.aru.db.entities.user.UserProfile
import pw.aru.db.entities.user.UserRelationship
import pw.aru.utils.Colors.blurple
import pw.aru.utils.extensions.lib.inlineField
import pw.aru.utils.extensions.lib.sendEmbed
import pw.aru.utils.text.*
import java.time.format.DateTimeFormatter

@Command("profile")
class ProfileCmd(private val db: AruDB) : ICommand {
    override val category = Category.SOCIAL

    private val dtf = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    override fun CommandContext.call() {
        val args = parseable()

        val member = args.tryTakeMember(guild) ?: author

        if (member.user().bot()) {
            channel.sendMessage("$X Bots don't have profiles.")
            return
        }

        val profile = UserProfile(db, member.idAsLong())

        channel.sendEmbed {
            author("${member.effectiveName()}'s Profile (BETA Feature)")
            thumbnail(member.user().effectiveAvatarUrl())
            color(blurple)

            inlineField("$ARUYEN AruYens", "¥ %,d".format(profile.money))
            inlineField("$PREMIUMYEN Credits", "₢ %,d".format(profile.premiumMoney))
            inlineField("$REP Reputation", "%,d rep".format(profile.rep))
            inlineField("$LVL Experience", "LVL %,d | %,d XP".format(profile.lvl, profile.xp))
            inlineField(
                "$CAKE Birthday",
                profile.bday?.let { dtf.format(it.toInstant()) } ?: "Not set."
            )

            val rel = profile.relationship?.let { it.status to it.displayString(author) }

            if (rel == null) {
                inlineField("$BLUE_HEART Single", "_You can ask them out._")
            } else {
                val (status, display) = rel
                inlineField(
                    "${if (status == UserRelationship.Status.DIVORCED) BROKEN_HEART else HEART} Relationship Status",
                    display
                )
            }
        }
    }
}