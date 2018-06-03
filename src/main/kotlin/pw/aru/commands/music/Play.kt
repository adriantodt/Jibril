package pw.aru.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.core.Permission.MESSAGE_ADD_REACTION
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.music.MusicPermissionCommand.Companion.checkPermissions
import pw.aru.core.categories.Categories
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.music.AudioRequester
import pw.aru.core.music.MusicManager
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR2
import pw.aru.utils.emotes.STOP
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X
import pw.aru.utils.extensions.showHelp
import pw.aru.utils.extensions.withPrefix
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

sealed class PlayCommand(
    private val musicManager: MusicManager,
    private val force: Boolean,
    private val next: Boolean,
    private val playNow: Boolean
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
                request(event, it.url, if (isDev) musicManager.devPlayerManager else musicManager.httpSafePlayerManager)
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
                    "cdn.discordapp.com", "media.discordapp.com" -> request(event, args, musicManager.httpSafePlayerManager)

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
            !force, next, playNow
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
@UseFullInjector
class Play(musicManager: MusicManager) : PlayCommand(musicManager, false, false, false) {
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

        seeAlso("playnow", "playnext", "forceplay", "forceplaynow", "forceplaynext")
    }
}

@Command("forceplay", "fp")
@UseFullInjector
class ForcePlay(musicManager: MusicManager) : PlayCommand(musicManager, true, false, false) {
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
        usage("forceplay [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplay <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        seeAlso("play", "playnow", "playnext", "forceplaynow", "forceplaynext")
    }
}

@Command("playnext", "pn")
@UseFullInjector
class PlayNext(musicManager: MusicManager) : PlayCommand(musicManager, false, true, false) {
    override val helpHandler = HelpFactory("PlayNext Command") {
        aliases("pn")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        )

        usage("playnext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("playnext <song url>", "Loads and plays the song from the URL.")
        usage("playnext [youtube/yt] <search term>", "Searches for the video in Youtube.")
        usage("playnext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        seeAlso("play", "playnow", "forceplay", "forceplaynow", "forceplaynext")
    }
}

@Command("forceplaynext", "fpn")
@UseFullInjector
class ForcePlayNext(musicManager: MusicManager) : PlayCommand(musicManager, true, true, false) {
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
        usage("forceplaynext [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplaynext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        seeAlso("play", "playnow", "playnext", "forceplay", "forceplaynow")
    }
}

@Command("playnow")
@UseFullInjector
class PlayNow(private val musicManager: MusicManager) : PlayCommand(musicManager, false, true, true) {

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (checkPermissions(event, musicManager.getMusicPlayer(event.guild), true)) {
            super.call(event, args)
        } else {
            event.channel.sendMessage(
                "$STOP B-baka, I'm not allowed to let you do that!\n\n$THINKING Maybe you meant ``${"playnext".withPrefix()}`` instead?"
            ).queue()
        }
    }

    override val helpHandler = HelpFactory("PlayNow Command") {
        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        )

        usage("playnext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("playnext <song url>", "Loads and plays the song from the URL.")
        usage("playnext [youtube/yt] <search term>", "Searches for the video in Youtube.")
        usage("playnext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        seeAlso("play", "playnext", "forceplay", "forceplaynow", "forceplaynext")
    }
}

@Command("forceplaynow")
@UseFullInjector
class ForcePlayNow(private val musicManager: MusicManager) : PlayCommand(musicManager, true, true, true) {

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        if (checkPermissions(event, musicManager.getMusicPlayer(event.guild), true)) {
            super.call(event, args)
        } else {
            event.channel.sendMessage(
                "$STOP B-baka, I'm not allowed to let you do that!\n\n$THINKING Maybe you meant ``${"forceplaynext".withPrefix()}`` instead?"
            ).queue()
        }
    }

    override val helpHandler = HelpFactory("ForcePlayNow Command") {
        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        )

        usage("forceplaynext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("forceplaynext <song url>", "Loads and plays the song from the URL.")
        usage("forceplaynext [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplaynext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        seeAlso("play", "playnow", "playnext", "forceplay", "forceplaynext")
    }
}