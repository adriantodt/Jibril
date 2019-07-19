package pw.aru.commands.music

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.music.MusicSystem
import pw.aru.core.permissions.MemberPermissions.ADMIN
import pw.aru.core.permissions.MemberPermissions.DJ
import pw.aru.core.permissions.Permissions
import pw.aru.core.permissions.UserPermissions.BOT_DEVELOPER

@Command("resetmusic")
class ResetMusic(val musicSystem: MusicSystem) : ICommand, ICommand.Permission {
    override val category = Category.MUSIC

    override val permissions = Permissions.AnyOf(DJ, ADMIN, BOT_DEVELOPER)

    override fun CommandContext.call() {
        val player = musicSystem.players.remove(guild.idAsLong())

        if (player == null) {
            send("MusicPlayer already reset.")
            return
        }

        player.andePlayer.runCatching { destroy() }
        player.runCatching { destroy() }

        send("MusicPlayer was reset. If this don't fix music, ask for support over at `aru!hangout`.")
    }
}