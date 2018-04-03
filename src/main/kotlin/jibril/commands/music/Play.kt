package jibril.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.Command
import jibril.core.commands.CommandPermission
import jibril.core.commands.ICommand
import jibril.core.music.AudioRequester
import jibril.core.music.MusicManager
import jibril.utils.commands.HelpFactory
import jibril.utils.emotes.ERROR2
import jibril.utils.emotes.X
import jibril.utils.extensions.showHelp
import jibril.utils.extensions.withPrefix
import net.dv8tion.jda.core.Permission.MESSAGE_ADD_REACTION
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

sealed class PlayCommand(
    private val musicManager: MusicManager,
    private val force: Boolean,
    private val next: Boolean
) : ICommand, ICommand.HelpDialogProvider {
    override val category: Category = Categories.MUSIC

    private val replacers = listOf(
        listOf("soundcloud:", "soundcloud ", "sc:", "sc ") to "scsearch:",
        listOf("youtube:", "youtube ", "yt:", "yt ") to "ytsearch:"
    )

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (!event.member.voiceState.inVoiceChannel()) {
            event.channel.sendMessage("$X You need to be connected to a Voice Channel to use this command!").queue()
            return
        }

        if (!event.guild.selfMember.hasPermission(event.channel, MESSAGE_ADD_REACTION)) {
            event.channel.sendMessage("$X Hey, I need the **${MESSAGE_ADD_REACTION.name}** permission in order to do that!").queue()
            return
        }

        val isDev = CommandPermission.BOT_DEVELOPER.test(event.member)

        if (args.isEmpty()) {
            val attachments = event.message.attachments.filter { !it.isImage }
            if (attachments.isEmpty()) return showHelp()

            attachments.forEach {
                request(event, it.url, if (isDev) musicManager.devPlayerManager else musicManager.attachmentPlayerManager)
            }
        } else {
            val playerManager = if (isDev) musicManager.devPlayerManager else musicManager.userPlayerManager

            for ((list, replacement) in replacers) {
                for (prefix in list) {
                    if (args.startsWith(prefix)) {
                        request(event, replacement + args.substring(prefix.length).trim(), playerManager)
                        return
                    }
                }
            }

            try {
                when (URL(args).host) {
                    "cdn.discordapp.com" -> request(event, args, musicManager.attachmentPlayerManager)
                    else -> request(event, args, playerManager)
                }
            } catch (e: Exception) {
                request(event, "ytsearch: ${args.trim()}", playerManager)
            }
        }
    }

    private fun request(event: GuildMessageReceivedEvent, args: String, playerManager: AudioPlayerManager) {
        val future = AudioRequester.loadAndPlay(
            event.channel, event.member,
            musicManager.getMusicPlayer(event.guild),
            args,
            playerManager,
            !force, next
        )

        try {
            future.get(1, TimeUnit.MINUTES)
        } catch (e: TimeoutException) {
            future.cancel(true)
            event.channel.sendMessage("$ERROR2 The music search took too long. If this keeps happening, go to our Support Server.").queue()
            AudioRequester.logger.warn("Took too long to get results from LavaPlayer.")
        }
    }
}

@Command("play", "p")
class Play
@Inject constructor(musicManager: MusicManager) : PlayCommand(musicManager, false, false) {
    override val helpHandler = HelpFactory("Play Command") {
        aliases("p")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing another song, the song will be added to the end of the queue."
        )

        usage("play", "+ attachment", "Loads and plays the song from the attachment.")
        usage("play <song url>", "Loads and plays the song from the URL.")
        usage("play [youtube/yt] <search term>", "Searches for the video in Youtube.")
        usage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        alsoSee("forceplay", "Similar to `${"play".withPrefix()}`, but doesn't open a dialog on search results.")
        alsoSee("playnext", "Similar to `${"play".withPrefix()}`, but add the musics to the begin of the queue.")
        alsoSee("forceplaynext", "A mix of the `${"forceplay".withPrefix()}` and `${"playnext".withPrefix()}` commands.")

    }
}

@Command("forceplay", "fp")
class ForcePlay
@Inject constructor(musicManager: MusicManager) : PlayCommand(musicManager, true, false) {
    override val helpHandler = HelpFactory("ForcePlay Command") {
        aliases("fp")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing another song, the song will be added to the end of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        )

        usage("forceplay", "+ attachment", "Loads and plays the song from the attachment.")
        usage("forceplay <song url>", "Loads and plays the song from the URL.")
        usage("forceplay <youtube/yt> <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplay <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        alsoSee("play", "Similar to `${"forceplay".withPrefix()}`, but opens a dialog on search results.")
        alsoSee("playnext", "Similar to `${"play".withPrefix()}`, but add the musics to the begin of the queue.")
        alsoSee("forceplaynext", "A mix of the `${"forceplay".withPrefix()}` and `${"playnext".withPrefix()}` commands.")
    }
}

@Command("playnext", "pn")
class PlayNext
@Inject constructor(musicManager: MusicManager) : PlayCommand(musicManager, false, true) {
    override val helpHandler = HelpFactory("PlayNext Command") {
        aliases("pn")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        )

        usage("playnext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("playnext <song url>", "Loads and plays the song from the URL.")
        usage("playnext <youtube/yt> <search term>", "Searches for the video in Youtube.")
        usage("playnext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        alsoSee("play", "Similar to `${"playnext".withPrefix()}`, but add the musics to the end of the queue.")
        alsoSee("forceplay", "Similar to `${"play".withPrefix()}`, but doesn't open a dialog on search results.")
        alsoSee("forceplaynext", "A mix of the `${"forceplay".withPrefix()}` and `${"playnext".withPrefix()}` commands.")
    }
}

@Command("forceplaynext", "fpn")
class ForcePlayNext
@Inject constructor(musicManager: MusicManager) : PlayCommand(musicManager, true, true) {
    override val helpHandler = HelpFactory("ForcePlayNext Command") {
        aliases("fpn")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        )

        usage("forceplaynext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("forceplaynext <song url>", "Loads and plays the song from the URL.")
        usage("forceplaynext <youtube/yt> <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplaynext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        seeAlso("play", "forceplay", "forceplaynext")
    }
}