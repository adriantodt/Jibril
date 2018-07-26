package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.VOLUME

@Command("volume", "vol")
@UseFullInjector
class Volume(musicManager: MusicManager) : MusicCommand(musicManager), ICommand.HelpDialogProvider {
    private val setter = SetVolume(musicManager)

    override fun CommandContext.call(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
        if (args.isEmpty()) {
            event.channel.sendMessage("$VOLUME Volume: **${musicPlayer.audioPlayer.volume}/150**").queue()
            return
        }
        with(setter) {
            call(musicPlayer, currentTrack)
        }
    }

    override val helpHandler = HelpFactory("Volume Command") {
        aliases("vol")

        description(
            "Gets or sets the current volume.",
            "",
            "To be able to set the volume, you have to:",
            "- The only user one listening to me",
            "- Have either DJ or Server Admin permissions"
        )

        usage("volume", "Gets the current volume.")
        usage("volume <1-150>", "Sets the current volume.")

        seeAlso("play", "queue", "pause")
    }

    private class SetVolume(musicManager: MusicManager) : MusicPermissionCommand(musicManager) {
        override fun CommandContext.actionWithPerms(musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack) {
            val volume = (args.toIntOrNull() ?: return showHelp()).coerceIn(1, 150)

            musicPlayer.audioPlayer.volume = volume

            event.channel.sendMessage("$SUCCESS Volume set to **$volume/150**.").queue()
        }
    }
}