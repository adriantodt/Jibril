package pw.aru.core

import com.mewna.catnip.entity.message.Message
import pw.aru.Aru
import pw.aru.core.commands.ICommand
import pw.aru.core.patreon.Patreon
import pw.aru.core.permissions.Permission
import pw.aru.db.AruDB
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.STOP
import pw.aru.utils.text.THINKING

class CommandChecks(private val aru: Aru, private val db: AruDB) {

    fun runChecks(message: Message, command: ICommand, userPerms: Set<Permission>): Boolean {
        if (aru == Aru.PATREON && !Patreon.patreonBotGuildCheck(db, message.guild()!!)) {
            message.channel().sendMessage(
                multiline(
                    "$ERROR B-baka, this server is not premium!",
                    "I'm a Patreon-only Bot, that means you gotta donate to me so I can provide you lag-free music and other shenanigans.",
                    "",
                    "$THINKING Interested on having access? Donate \$4 or more on `patreon.aru.pw` and join the support server!",
                    "**Do you think this is this an error?  Join our server on `support.aru.pw` and contact AdrianTodt#0722**."
                )
            )
            return false
        }

        if (command is ICommand.Permission && !command.permissions.check(userPerms)) {
            message.channel().sendMessage(
                multiline(
                    "$STOP B-baka, I'm not allowed to let you do that!",
                    "",
                    "You need the following permissions:",
                    command.permissions.toString().capitalize()
                )
            )
            return false
        }

        return true
    }

}