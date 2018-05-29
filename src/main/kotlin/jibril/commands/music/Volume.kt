package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import jibril.core.commands.Command
import jibril.core.commands.ICommand
import jibril.core.music.GuildMusicPlayer
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.SUCCESS
import jibril.utils.emotes.VOLUME
import jibril.utils.extensions.showHelp
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

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