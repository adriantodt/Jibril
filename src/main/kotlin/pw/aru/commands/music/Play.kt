package pw.aru.commands.music

import bsh.ParserConstants.BANG
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.core.Permission.MESSAGE_ADD_REACTION
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.commands.music.MusicPermissionCommand.Companion.checkPermissions
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.UseFullInjector
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.GuildMusicPlayer.RepeatMode.*
import pw.aru.core.music.MusicManager
import pw.aru.core.music.MusicRequester
import pw.aru.core.music.MusicRequester.Companion.loadAndPlay
import pw.aru.core.parser.parseOptions
import pw.aru.core.parser.tryTakeInt
import pw.aru.utils.emotes.ERROR2
import pw.aru.utils.emotes.STOP
import pw.aru.utils.emotes.THINKING
import pw.aru.utils.emotes.X
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

sealed class PlayCommand(
    private val musicManager: MusicManager,
    private val force: Boolean,
    private val next: Boolean,
    private val playNow: Boolean,
    override val helpHandler: Help
) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.MUSIC

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
                "$STOP B-baka, I'm not allowed to let you do that!\n\n$THINKING Maybe you meant ``$prefix${if (force) "forceplaynext" else "playnext"}`` instead?"
            ).queue()
            return
        }

        var shufflePlaylist = false

        args.parseOptions {
            option("--volume", "--vol", "-v") {
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

            option("--repeat", "-r") {
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

            option("--shuffled", "-s") {
                shufflePlaylist = true
            }
        }

        val isDev = CommandPermission.BOT_DEVELOPER.test(event.member)

        if (args.isEmpty()) {
            val attachments = event.message.attachments.filter { !it.isImage }
            if (attachments.isEmpty()) return showHelp()

            attachments.forEach {
                request(event, it.url, if (isDev) musicManager.devPlayerManager else musicManager.httpSafePlayerManager, shufflePlaylist)
            }
        } else {
            val playerManager = if (isDev) musicManager.devPlayerManager else musicManager.userPlayerManager
            val music = args.takeRemaining()

            for ((list, replacement) in replacers) {
                for (prefix in list) {
                    if (music.startsWith(prefix)) {
                        request(event, replacement + music.substring(prefix.length).trim(), playerManager, shufflePlaylist)
                        return
                    }
                }
            }

            try {
                when (URL(music).host) {
                    "cdn.discordapp.com", "media.discordapp.com" -> request(event, music, musicManager.httpSafePlayerManager, shufflePlaylist)
                    else -> request(event, music, playerManager, shufflePlaylist)
                }
            } catch (e: Exception) {
                request(event, "ytsearch: ${music.trim()}", playerManager, shufflePlaylist)
            }
        }
    }

    private fun request(event: GuildMessageReceivedEvent, args: String, playerManager: AudioPlayerManager, shufflePlaylist: Boolean) {
        val future = loadAndPlay(
            event.channel, event.member,
            musicManager[event.guild],
            args,
            playerManager,
            !force, next, playNow, shufflePlaylist
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
class Play(musicManager: MusicManager) : PlayCommand(
    musicManager, false, false, false,
    Help(
        CommandDescription(listOf("play", "p"), "Play Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing another song, the song will be added to the end of the queue."
        ),
        Usage(
            CommandUsage("play", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("play <song url>", "Loads and plays the song from the URL."),
            CommandUsage("play [youtube/yt] <search term>", "Searches for the video in Youtube."),
            CommandUsage("play <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["playnow", "playnext", "forceplay", "forceplaynow", "forceplaynext"]
    )
)

@Command("forceplay", "fp")
@UseFullInjector
class ForcePlay(musicManager: MusicManager) : PlayCommand(
    musicManager, true, false, false,
    Help(
        CommandDescription(listOf("forceplay", "fp"), "ForcePlay Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing another song, the song will be added to the end of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplay", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplay <song url>", "Loads and plays the song from the URL."),
            CommandUsage("forceplay [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result."),
            CommandUsage("forceplay <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["play", "playnow", "playnext", "forceplaynow", "forceplaynext"]
    )
)

@Command("playnow", "pn")
@UseFullInjector
class PlayNow(musicManager: MusicManager) : PlayCommand(
    musicManager, false, true, true,
    Help(
        CommandDescription(listOf("playnow", "pn"), "PlayNow Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        ),
        Usage(
            CommandUsage("playnow", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("playnow <song url>", "Loads and plays the song from the URL."),
            CommandUsage("playnow [youtube/yt] <search term>", "Searches for the video in Youtube."),
            CommandUsage("playnow <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["play", "playnext", "forceplay", "forceplaynow", "forceplaynext"]
    )
)

@Command("forceplaynow", "fpn")
@UseFullInjector
class ForcePlayNow(musicManager: MusicManager) : PlayCommand(
    musicManager, true, true, true,
    Help(
        CommandDescription(listOf("forceplaynow", "fpn"), "ForcePlayNow Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplaynow", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplaynow <song url>", "Loads and plays the song from the URL."),
            CommandUsage("forceplaynow [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result."),
            CommandUsage("forceplaynow <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["play", "playnow", "playnext", "forceplay", "forceplaynext"]
    )
)

@Command("playnext")
@UseFullInjector
class PlayNext(musicManager: MusicManager) : PlayCommand(
    musicManager, false, true, false,
    Help(
        CommandDescription(listOf("playnext"), "PlayNext Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue."
        ),
        Usage(
            CommandUsage("playnext", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("playnext <song url>", "Loads and plays the song from the URL."),
            CommandUsage("playnext [youtube/yt] <search term>", "Searches for the video in Youtube."),
            CommandUsage("playnext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["play", "playnow", "forceplay", "forceplaynow", "forceplaynext"]
    )
)

@Command("forceplaynext")
@UseFullInjector
class ForcePlayNext(musicManager: MusicManager) : PlayCommand(
    musicManager, true, true, false,
    Help(
        CommandDescription(listOf("forceplaynext"), "ForcePlayNext Command", thumbnail = "https://assets.aru.pw/img/category/music.png"),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplaynext", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplaynext <song url>", "Loads and plays the song from the URL."),
            CommandUsage("forceplaynext [youtube/yt] <search term>", "Searches for the video in Youtube and adds the first result."),
            CommandUsage("forceplaynext <soundcloud/sc> <search term>", "Searches for the song in SoundCloud and adds the first result.")
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage("play --shuffled <...>", "Shuffles the order of the playlist added. Has no effects on single tracks."),
            "(You need the permissions to set the volume or the repeat mode)",
            "",
            "**Aliases**:",
            " - Volume: `--vol`, `-v`",
            " - Repeat: `-r`",
            " - Shuffle Playlists: `-s`"
        ),
        SeeAlso["play", "playnow", "playnext", "forceplay", "forceplaynow"]
    )
)