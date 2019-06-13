package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.commands.music.base.MusicCommand
import pw.aru.commands.music.base.MusicPermissionCommand
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.events.ChangeVolumeEvent
import pw.aru.core.patreon.Patreon
import pw.aru.core.permissions.UserPermissions
import pw.aru.db.AruDB
import pw.aru.utils.extensions.lang.multiline
import pw.aru.utils.text.ERROR
import pw.aru.utils.text.VOLUME

@Command("volume", "vol")
class Volume(musicSystem: MusicSystem, db: AruDB) : MusicCommand(musicSystem), ICommand.HelpDialogProvider {
    private val setter = SetVolume(musicSystem, db)

    override fun CommandContext.call(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (args.isEmpty()) {
            send("$VOLUME Volume: **${musicPlayer.andePlayer.volume()}/150**")
            return
        }
        with(setter) {
            call(musicPlayer, currentTrack)
        }
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("volume", "vol"),
            "Volume Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Gets or sets the current volume.",
            "",
            "To be able to set the volume, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        ),
        Usage(
            CommandUsage("volume", "Gets the current volume."),
            CommandUsage("volume <1-150>", "Sets the current volume.")
        ),
        SeeAlso["play", "queue", "pause"]
    )

    private class SetVolume(musicSystem: MusicSystem, private val db: AruDB) :
        MusicPermissionCommand(musicSystem, userQueued = true) {
        override fun CommandContext.actionWithPerms(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
            if (!permissions.contains(UserPermissions.BOT_DEVELOPER) && !Patreon.isPremium(db, author)) {
                send(
                    multiline(
                        "$ERROR This is a premium-only feature." +
                                "In order to get premium benefits like this one, consider donating and being one of our patreons (`aru!links`).",
                        "If you donate, join the support server and ask for the Premium privileges. Thanks for understanding~"
                    )
                )

                return
            }

            val volume = (args.toIntOrNull() ?: return showHelp()).coerceIn(1, 150)
            musicPlayer.publish(ChangeVolumeEvent(asMusicSource(), volume))
        }
    }
}