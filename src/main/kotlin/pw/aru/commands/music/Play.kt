package pw.aru.commands.music

import bsh.ParserConstants.BANG
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
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.music.GuildMusicPlayer.RepeatMode.*
import pw.aru.core.music.MusicManager
import pw.aru.core.music.MusicRequester
import pw.aru.core.parser.parseOptions
import pw.aru.core.parser.tryTakeInt
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.ERROR2
import pw.aru.utils.emotes.STOP
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X
import pw.aru.utils.extensions.usage
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

    override fun CommandContext.call() {
        val args = parseable()

        if (!event.member.voiceState.inVoiceChannel()) {
            send("$X You need to be connected to a Voice Channel to use this command!").queue()
            return
        }

        if (!event.guild.selfMember.hasPermission(event.channel, MESSAGE_ADD_REACTION)) {
            send("$X Hey, I need the **${MESSAGE_ADD_REACTION.name}** permission in order to do that!").queue()
            return
        }

        val musicPlayer = musicManager[event.guild]
        if (playNow && !checkPermissions(event, musicPlayer, true)) {
            send(
                "$STOP B-baka, I'm not allowed to let you do that!\n\n$THINKING Maybe you meant ``${(if (force) "forceplaynext" else "playnext").withPrefix()}`` instead?"
            ).queue()
            return
        }

        args.parseOptions {
            option("--vol") {
                val vol = tryTakeInt()
                if (vol != null) {
                    if (!checkPermissions(event, musicPlayer, true)) {
                        send(
                            "$BANG Volume will not be changed since as you don't have the permission to change it."
                        ).queue()
                    } else {
                        musicPlayer.audioPlayer.volume = vol
                    }
                }
            }

            option("--repeat") {
                val repeat = takeString()
                if (!checkPermissions(event, musicPlayer, false)) {
                    send(
                        "$BANG Repeat mode will not be changed since as you don't have the permission to change it."
                    ).queue()
                } else {
                    val mode = when (repeat) {
                        "none", "disable", "false", "n" -> NONE
                        "song", "music", "current", "playing", "true", "s" -> SONG
                        "queue", "playlist", "list", "q" -> QUEUE
                        else -> null
                    }

                    if (mode != null) musicPlayer.repeatMode = mode
                }
            }
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
            val music = args.takeRemaining()

            for ((list, replacement) in replacers) {
                for (prefix in list) {
                    if (music.startsWith(prefix)) {
                        request(event, replacement + music.substring(prefix.length).trim(), playerManager)
                        return
                    }
                }
            }

            try {
                when (URL(music).host) {
                    "cdn.discordapp.com", "media.discordapp.com" -> request(event, music, musicManager.httpSafePlayerManager)
                    else -> request(event, music, playerManager)
                }
            } catch (e: Exception) {
                request(event, "ytsearch: ${music.trim()}", playerManager)
            }
        }
    }

    private fun request(event: GuildMessageReceivedEvent, args: String, playerManager: AudioPlayerManager) {
        val future = MusicRequester.loadAndPlay(
            event.channel, event.member,
            musicManager[event.guild],
            args,
            playerManager,
            !force, next, playNow
        )

        try {
            future.get(1, TimeUnit.MINUTES)
        } catch (e: TimeoutException) {
            future.cancel(true)
            event.channel.sendMessage("$ERROR2 The music search took too long. If this keeps happening, go to our Support Server.").queue()
            MusicRequester.logger.warn("Took too long to get results from LavaPlayer.")
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

        note(
            "**Magic Prefixes**:",
            "play --vol <volume> ...".usage("Sets the volume of the player."),
            "play --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

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

        note(
            "**Magic Prefixes**:",
            "forceplay --vol <volume> ...".usage("Sets the volume of the player."),
            "forceplay --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

        seeAlso("play", "playnow", "playnext", "forceplaynow", "forceplaynext")
    }
}

@Command("playnow", "pn")
@UseFullInjector
class PlayNow(musicManager: MusicManager) : PlayCommand(musicManager, false, true, true) {
    override val helpHandler = HelpFactory("PlayNow Command") {
        aliases("pn")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        )

        usage("playnow", "+ attachment", "Loads and plays the song from the attachment.")
        usage("playnow <song url>", "Loads and plays the song from the URL.")
        usage("playnow [youtube/yt] <search term>", "Searches for the video in Youtube.")
        usage("playnow <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        note(
            "**Magic Prefixes**:",
            "playnow --vol <volume> ...".usage("Sets the volume of the player."),
            "playnow --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

        seeAlso("play", "playnext", "forceplay", "forceplaynow", "forceplaynext")
    }
}

@Command("forceplaynow", "fpn")
@UseFullInjector
class ForcePlayNow(musicManager: MusicManager) : PlayCommand(musicManager, true, true, true) {

    override val helpHandler = HelpFactory("ForcePlayNow Command") {
        aliases("fpn")

        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        )

        usage("forceplaynow", "+ attachment", "Loads and plays the song from the attachment.")
        usage("forceplaynow <song url>", "Loads and plays the song from the URL.")
        usage("forceplaynow [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result.")
        usage("forceplaynow <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")

        note(
            "**Magic Prefixes**:",
            "forceplaynow --vol <volume> ...".usage("Sets the volume of the player."),
            "forceplaynow --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

        seeAlso("play", "playnow", "playnext", "forceplay", "forceplaynext")
    }
}

@Command("playnext")
@UseFullInjector
class PlayNext(musicManager: MusicManager) : PlayCommand(musicManager, false, true, false) {
    override val helpHandler = HelpFactory("PlayNext Command") {
        description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        )

        usage("playnext", "+ attachment", "Loads and plays the song from the attachment.")
        usage("playnext <song url>", "Loads and plays the song from the URL.")
        usage("playnext [youtube/yt] <search term>", "Searches for the video in Youtube.")
        usage("playnext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")

        note(
            "**Magic Prefixes**:",
            "playnext --vol <volume> ...".usage("Sets the volume of the player."),
            "playnext --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

        seeAlso("play", "playnow", "forceplay", "forceplaynow", "forceplaynext")
    }
}

@Command("forceplaynext")
@UseFullInjector
class ForcePlayNext(musicManager: MusicManager) : PlayCommand(musicManager, true, true, false) {
    override val helpHandler = HelpFactory("ForcePlayNext Command") {
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

        note(
            "**Magic Prefixes**:",
            "forceplaynext --vol <volume> ...".usage("Sets the volume of the player."),
            "forceplaynext --repeat <mode> ...".usage("Sets the repeat mode of the player."),
            "(You need the permissions to set the volume or the repeat mode)"
        )

        seeAlso("play", "playnow", "playnext", "forceplay", "forceplaynow")
    }
}
