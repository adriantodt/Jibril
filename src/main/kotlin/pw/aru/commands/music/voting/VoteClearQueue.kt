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
import pw.aru.utils.text.THINKING
import pw.aru.utils.text.X

@Command("voteclearqueue")
class VoteClearQueue(musicSystem: MusicSystem) : MusicActionCommand(musicSystem), ICommand.HelpDialogProvider {
    override fun CommandContext.action(musicPlayer: MusicPlayer, currentTrack: AudioTrack) {
        if (musicPlayer.queue.isEmpty()) {
            send(
                "$X The queue is already empty, silly!\n\n$THINKING Maybe you want to skip the current song with ``$prefix${"voteskip"}``, instead?"
            )
            return
        }

        musicPlayer.publish(ToggleVoteEvent(asMusicSource(), VoteType.CLEAR_QUEUE))
    }

    override val helpHandler = Help(
        CommandDescription(listOf("voteclearqueue"), "VoteClearQueue Command"),
        Description(
            "Create a poll to clear the player's queue.",
            "",
            "If 60% or more of the users listening vote, the player's queue will be queued."
        ),
        SeeAlso(
            CommandUsage(
                "clearqueue",
                "Clears the queue of the player without requiring voting."
            ),
            CommandUsage("skip", "Skips the current song."),
            CommandUsage("voteskip", "Create a poll to skip the current song.")
        )
    )
}