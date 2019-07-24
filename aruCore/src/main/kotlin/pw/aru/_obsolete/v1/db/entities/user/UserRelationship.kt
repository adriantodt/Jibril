package pw.aru._obsolete.v1.db.entities.user

import com.mewna.catnip.entity.guild.Member
import pw.aru._obsolete.v1.db.AruDB
import pw.aru._obsolete.v1.db.base.RedisField
import pw.aru._obsolete.v1.db.base.RedisObject
import pw.aru._obsolete.v1.db.base.Serializer.Companion.enum
import pw.aru._obsolete.v1.db.base.annotations.R
import java.text.MessageFormat

@R("pw.aru:user:relationship")
class UserRelationship(db: AruDB, id: Long) : RedisObject(db, id) {
    var user1 by RedisField.Long(0)
    var user2 by RedisField.Long(0)

    var status by RedisField(enum(), Status.NOTHING)

    enum class Status(insider: String, outsider: String) {
        NOTHING("N-Nothing at all!", "N-Nothing at all!"),
        DATING("Dating {0}.", "{0} and {1} are Dating."),
        MARRIED("Married to {0}.", "{0} and {1} are Married."),
        DIVORCED("Divorced from {0}.", "{0} and {1} are Divorced.");

        val insideFormat = MessageFormat(insider)
        val outsideFormat = MessageFormat(outsider)
    }

    fun displayString(member: Member): String {
        val u1 = user1
        val u2 = user2
        val s = status

        when (member.idAsLong()) {
            u1 -> {
                val other = member.guild().member(u2)?.effectiveName()
                    ?: member.catnip()?.cache()?.user(u2)?.username()
                    ?: "an unknown user"

                return s.insideFormat.format(arrayOf(other))
            }
            u2 -> {
                val other = member.guild().member(u1)?.effectiveName()
                    ?: member.catnip()?.cache()?.user(u1)?.username()
                    ?: "an unknown user"

                return s.insideFormat.format(arrayOf(other))
            }
            else -> {
                val name1 = member.guild().member(u1)?.effectiveName()
                    ?: member.catnip()?.cache()?.user(u1)?.username()
                    ?: "an unknown user"

                val name2 = member.guild().member(u2)?.effectiveName()
                    ?: member.catnip()?.cache()?.user(u2)?.username()
                    ?: "an unknown user"

                return s.outsideFormat.format(arrayOf(name1, name2))
            }
        }
    }

}