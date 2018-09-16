package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.db.AruDB
import pw.aru.db.entities.guild.GuildSettings
import pw.aru.db.entities.user.UserSettings
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.VOLUME
import pw.aru.utils.extensions.idLong
import pw.aru.utils.extensions.multiline

@Command("volume", "vol")
@UseFullInjector
class Volume(musicManager: MusicManager, db: AruDB) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    private val setter = SetVolume(musicManager, db)

    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (args.isEmpty()) {
            send("$VOLUME Volume: **${musicPlayer.audioPlayer.volume}/150**").queue()
            return
        }
        with(setter) {
            call(musicPlayer, currentTrack)
        }
    }

    override val helpHandler = Help(
        CommandDescription(listOf("volume", "vol"), "Volume Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),
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

    private class SetVolume(musicManager: MusicManager, private val db: AruDB) : MusicPermissionCommand(musicManager, userQueued = true) {
        override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
            if (!GuildSettings(db, guild.idLong).legacyPremium && !UserSettings(db, author.idLong).legacyPremium) {
                send(
                    multiline(
                        "$ERROR This is a premium-only feature." +
                            "In order to get premium benefits like this one, consider donating and being one of our patreons (`aru!links`).",
                        "If you donate, join the support server and ask for the Premium privileges. Thanks for understanding~"
                    )
                ).queue()

                return
            }

            val volume = (args.toIntOrNull() ?: return showHelp()).coerceIn(1, 150)
            musicPlayer.audioPlayer.volume = volume
            send("$SUCCESS Volume set to **$volume/150**.").queue()
        }
    }
}