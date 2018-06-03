package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.music.GuildMusicPlayer
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.emotes.VOLUME
import pw.aru.utils.extensions.showHelp

@Command("volume", "vol")
class Volume
(musicManager: MusicManager) : MusicPermissionCommand(musicManager), ICommand.HelpDialogProvider {

    override fun run(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        if (args.isEmpty()) {
            event.channel.sendMessage("$VOLUME Volume: **${musicPlayer.audioPlayer.volume}/150**").queue()
            return
        }
        super.run(event, musicPlayer, currentTrack, args)
    }

    override fun action(event: GuildMessageReceivedEvent, musicPlayer: GuildMusicPlayer, currentTrack: AudioTrack, args: String) {
        val volume = (args.toIntOrNull() ?: return showHelp()).coerceIn(1, 150)

        musicPlayer.audioPlayer.volume = volume

        event.channel.sendMessage("$SUCCESS Volume set to **$volume/150**.").queue()
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
}