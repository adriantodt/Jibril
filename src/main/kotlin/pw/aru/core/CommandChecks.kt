package pw.aru.core

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.Aru
import pw.aru.core.commands.ICommand
import pw.aru.core.patreon.Patreon
import pw.aru.db.AruDB
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.STOP
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.extensions.multiline

class CommandChecks(private val aru: Aru, private val db: AruDB) {
    fun runChecks(event: GuildMessageReceivedEvent, command: ICommand): Boolean {
        if (aru == Aru.PATREON && !Patreon.patreonBotGuildCheck(db, event.guild)) {
            event.channel.sendMessage(
                multiline(
                    "$ERROR B-baka, this server is not premium!",
                    "I'm a Patreon-only Bot, that means you gotta donate to me so I can provide you lag-free music and other shenanigans.",
                    "",
                    "$THINKING Interested on having access? Donate \$4 or more on `patreon.aru.pw` and join the support server!",
                    "**Do you think this is this an error?  Join our server on `support.aru.pw` and contact AdrianTodt#0722**."
                )
            ).queue()
            return false
        }

        if (command is ICommand.Permission && !command.permission.test(event.member)) {
            event.channel.sendMessage("$STOP B-baka, I'm not allowed to let you do that!").queue()
            return false
        }

        return true
    }

}