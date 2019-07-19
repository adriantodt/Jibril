package pw.aru.commands.music.voting

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import pw.aru.bot.commands.Command
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.*
import pw.aru.bot.music.MusicPlayer
import pw.aru.bot.music.MusicSystem
import pw.aru.bot.music.entities.VoteType
import pw.aru.bot.music.events.ToggleVoteEvent
import pw.aru.commands.music.base.MusicActionCommand

@Command("voteskip")
class VoteSkip(musicSystem: MusicSystem) : MusicActionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        musicPlayer.publish(ToggleVoteEvent(asMusicSource(), VoteType.SKIP))
    }

    override val helpHandler = Help(
        CommandDescription(
            listOf("voteskip"),
            "VoteSkip Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),
        Description(
            "Create a poll to skip the current track.",
            "",
            "If 60% or more of the users listening vote, the current track will be skipped."
        ),
        SeeAlso(
            CommandUsage("skip", "Skips the track without requiring voting.")
        )
    )
}