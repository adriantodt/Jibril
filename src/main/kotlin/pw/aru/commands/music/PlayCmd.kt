package pw.aru.commands.music

import com.mewna.catnip.entity.util.Permission.ADD_REACTIONS
import pw.aru.commands.music.base.MusicPermissionCommand.Companion.checkPermissions
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.core.music.MusicPlayer
import pw.aru.core.music.MusicSystem
import pw.aru.core.music.entities.*
import pw.aru.core.music.events.LoadItemEvent
import pw.aru.core.parser.Args
import pw.aru.core.parser.parseAndCreate
import pw.aru.core.parser.tryTakeInt
import pw.aru.core.parser.tryTakeTimeMillis
import pw.aru.core.patreon.Patreon
import pw.aru.core.permissions.UserPermissions
import pw.aru.utils.text.BANG
import pw.aru.utils.text.X
import java.util.*

sealed class PlayCmd(
    val musicSystem: MusicSystem,
    val defaultLoadItemMode: LoadItemMode,
    val enqueueLoadMode: EnqueueLoadMode,
    override val helpHandler: Help
) : ICommand, ICommand.HelpDialogProvider {
    override val category = Category.MUSIC

    private val replacers = listOf(
        listOf("soundcloud:", "soundcloud ", "sc:", "sc ") to "scsearch:",
        listOf("youtube:", "youtube ", "yt:", "yt ") to "ytsearch:"
    )

    override fun CommandContext.call() {
        val args = parseable()
        val attachments = message.attachments()


        if (catnip.cache().voiceState(guild.idAsLong(), author.idAsLong())?.channelIdAsLong() == null) {
            send("$X You need to be connected to a Voice Channel to use this command!")
            return
        }

        if (!self.hasPermissions(channel, ADD_REACTIONS)) {
            send("$X Hey, I need the **${ADD_REACTIONS.name}** permission in order to do that!")
            return
        }

        val musicPlayer = musicSystem[guild]

        val isDev = permissions.contains(UserPermissions.BOT_DEVELOPER)
        val isPatreon = Patreon.isPremium(musicSystem.db, author)

        val (loadItemMode, trackLoadOptions) = parseArgs(args, musicPlayer, isPatreon, isDev)

        if (!args.isEmpty()) {
            val sourceType = when {
                isDev -> ItemSource.DEV
                isPatreon -> ItemSource.PATREON
                else -> ItemSource.DEFAULT
            }

            val music = args.takeRemaining()

            for ((list, replacement) in replacers) {
                for (prefix in list) {
                    if (music.startsWith(prefix)) {
                        musicPlayer.publish(
                            LoadItemEvent(
                                asMusicSource(),
                                UUID.randomUUID(),
                                replacement + music.substring(prefix.length).trim(),
                                sourceType,
                                loadItemMode, trackLoadOptions
                            )
                        )
                        return
                    }
                }
            }

            musicPlayer.publish(
                LoadItemEvent(
                    asMusicSource(),
                    UUID.randomUUID(),
                    music,
                    sourceType,
                    loadItemMode, trackLoadOptions
                )
            )

        } else if (attachments.any { !it.image() }) {
            attachments.asSequence()
                .filterNot { it.image() }
                .forEach {
                    musicPlayer.publish(
                        LoadItemEvent(
                            asMusicSource(),
                            UUID.randomUUID(),
                            it.url(),
                            ItemSource.HTTP_SAFE,
                            loadItemMode, trackLoadOptions
                        )
                    )
                }
        } else {
            showHelp()
        }
    }

    private fun CommandContext.parseArgs(
        args: Args,
        player: MusicPlayer,
        patreon: Boolean,
        dev: Boolean
    ): Pair<LoadItemMode, TrackLoadOptions> {

        val loadItemMode = args.parseAndCreate<LoadItemMode> {
            val isChoose = option("--choose", "-c", value = true)
            val isForce = option("--force", "-f", value = true)

            creator {
                when {
                    isChoose.getOrDefault(false) -> LoadItemMode.CHOOSE
                    isForce.getOrDefault(false) -> LoadItemMode.FORCE
                    else -> defaultLoadItemMode
                }
            }
        }

        val trackLoadOptions = args.parseAndCreate<TrackLoadOptions> {
            val volume = option("--volume", "-vol", "-v") {
                tryTakeInt() ?: throw returnHelp()
            }

            val repeatMode = option("--repeat", "-r") {
                when (takeString()) {
                    "none", "disable", "false", "n" -> RepeatMode.NONE
                    "song", "music", "current", "playing", "true", "s" -> RepeatMode.SONG
                    "queue", "playlist", "list", "q" -> RepeatMode.QUEUE
                    else -> returnHelp()
                }
            }

            val shufflePlaylist = option("--shuffle", "-s", value = true)

            val start = option("--start") {
                tryTakeTimeMillis()
            }

            val end = option("--end") {
                tryTakeTimeMillis()
            }

            creator {
                val vol = volume.orNull
                val rep = repeatMode.orNull

                var nullVolume = false
                var nullRepeat = false

                if (vol != null) {
                    if (!dev && !patreon) {
                        nullVolume = true

                        send(
                            "$BANG Volume will not be changed as it's a premium-only feature."
                        )
                    } else if (!checkPermissions(player, false)) {
                        nullVolume = true

                        send(
                            "$BANG Volume will not be changed as you don't have the permission to change it."
                        )
                    }
                }

                if (rep != null && !checkPermissions(player, false)) {
                    nullRepeat = true

                    send(
                        "$BANG Repeat mode will not be changed since as you don't have the permission to change it."
                    )
                }

                TrackLoadOptions(
                    enqueueLoadMode,
                    shufflePlaylist.getOrDefault(false),
                    if (nullVolume) null else vol,
                    if (nullRepeat) null else rep,
                    start.orNull, end.orNull
                )
            }
        }

        return loadItemMode to trackLoadOptions
    }
}


@Command("play", "p")
class Play(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.DEFAULT, EnqueueLoadMode.DEFAULT,
    Help(
        CommandDescription(
            listOf("play", "p"),
            "Play Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

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
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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
class ForcePlay(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.FORCE, EnqueueLoadMode.DEFAULT,
    Help(
        CommandDescription(
            listOf("forceplay", "fp"),
            "ForcePlay Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing another song, the song will be added to the end of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplay", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplay <song url>", "Loads and plays the song from the URL."),
            CommandUsage(
                "forceplay [youtube/yt] <search term>",
                "Searches for the video in Youtube and adds the first result."
            ),
            CommandUsage(
                "forceplay <soundcloud/sc> <search term>",
                "Searches for the song in SoundCloud and adds the first result."
            )
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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
class PlayNow(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.DEFAULT, EnqueueLoadMode.NOW,
    Help(
        CommandDescription(
            listOf("playnow", "pn"),
            "PlayNow Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

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
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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
class ForcePlayNow(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.FORCE, EnqueueLoadMode.NOW,
    Help(
        CommandDescription(
            listOf("forceplaynow", "fpn"),
            "ForcePlayNow Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplaynow", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplaynow <song url>", "Loads and plays the song from the URL."),
            CommandUsage(
                "forceplaynow [youtube/yt] <search term>",
                "Searches for the video in Youtube and adds the first result."
            ),
            CommandUsage(
                "forceplaynow <soundcloud/sc> <search term>",
                "Searches for the song in SoundCloud and adds the first result."
            )
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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
class PlayNext(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.DEFAULT, EnqueueLoadMode.NEXT,
    Help(
        CommandDescription(
            listOf("playnext"),
            "PlayNext Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

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
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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
class ForcePlayNext(musicSystem: MusicSystem) : PlayCmd(
    musicSystem, LoadItemMode.FORCE, EnqueueLoadMode.NEXT,
    Help(
        CommandDescription(
            listOf("forceplaynext"),
            "ForcePlayNext Command",
            thumbnail = "https://assets.aru.pw/img/category/music.png"
        ),

        Description(
            "**Play songs!**",
            "If the user is in a voice channel and there's no song playing, I'll join your channel before starting.",
            "If I'm already playing a song, this command will add the song to the begin of the queue.",
            "Instead of opening a dialog in a search result, this command adds the first result instead."
        ),
        Usage(
            CommandUsage("forceplaynext", "+ attachment", "Loads and plays the song from the attachment."),
            CommandUsage("forceplaynext <song url>", "Loads and plays the song from the URL."),
            CommandUsage(
                "forceplaynext [youtube/yt] <search term>",
                "Searches for the video in Youtube and adds the first result."
            ),
            CommandUsage(
                "forceplaynext <soundcloud/sc> <search term>",
                "Searches for the song in SoundCloud and adds the first result."
            )
        ),
        Note(
            "**Magic Prefixes**:",
            commandUsage("play --volume <volume> <...>", "Sets the volume of the player."),
            commandUsage("play --repeat <mode> <...>", "Sets the repeat mode of the player."),
            commandUsage(
                "play --shuffled <...>",
                "Shuffles the order of the playlist added. Has no effects on single tracks."
            ),
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