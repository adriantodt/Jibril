package pw.aru.commands.actions.impl

import pw.aru.bot.categories.Category
import pw.aru.bot.commands.ICommand
import pw.aru.bot.commands.context.CommandContext
import pw.aru.bot.commands.help.CommandDescription
import pw.aru.bot.commands.help.CommandUsage
import pw.aru.bot.commands.help.Help
import pw.aru.bot.commands.help.Usage
import pw.aru.utils.extensions.lang.randomOf
import pw.aru.utils.extensions.lang.randomOrNull
import pw.aru.utils.extensions.lang.replaceEach
import pw.aru.utils.extensions.lang.toSmartString
import pw.aru.utils.extensions.lib.sendMessage

sealed class ImageBasedCommandImpl : ICommand, ICommand.HelpDialogProvider {
    abstract val names: List<String>
    abstract val commandName: String
    abstract val description: String
    abstract val provider: ImageProvider?
    abstract val nsfwProvider: ImageProvider?
    abstract val note: String?

    override fun CommandContext.call() {
        val sfw: ImageProvider? = provider
        val nsfw: ImageProvider? = nsfwProvider
        val provider: ImageProvider = when {
            sfw == null && nsfw == null -> {
                //ERROR
                throw IllegalStateException("Misconfigured command: ${names.first()}")
            }
            sfw != null && nsfw == null -> sfw
            sfw == null && nsfw != null -> {
                if (!requireNSFW()) return else nsfw
            }
            sfw != null && nsfw != null -> {
                //BEHAVIOR
                val args = parseable()
                if (!channel.nsfw()) sfw else if (args.takeString() == "nsfw") nsfw else randomOf(
                    sfw,
                    nsfw
                )
            }
            else -> throw RuntimeException("Impossible state")
        }

        val image = provider.provide()
        withMDC("command.uploadedImage" to image.meta) { handle(image) }
    }

    val cmdName get() = names[0]

    abstract fun CommandContext.handle(image: Image)
}

class ActionCommandImpl(
    override val names: List<String>,
    override val category: Category?,
    override val commandName: String,
    override val description: String,
    override val provider: ImageProvider?,
    override val nsfwProvider: ImageProvider?,
    override val note: String?,
    private val anyTarget: String,
    private val noTargets: String,
    private val targetsYou: String,
    private val targetsMe: String
) : ImageBasedCommandImpl() {
    override fun CommandContext.handle(image: Image) {
        val mentions = message.mentionedUsers()

        val f = when {
            mentions.isEmpty() -> noTargets
            mentions.all { it == message.member() } -> targetsYou
            mentions.all { it == message.guild()?.selfMember() } -> targetsMe
            else -> anyTarget
        }

        channel.sendMessage {
            addFile(image.fileName, image.inputStream())
            content(
                f.replaceEach(
                    "{author}" to "**${author.effectiveName(guild)}**",
                    "{mentions}" to mentions.toSmartString { "**${it.effectiveName(guild)}**" },
                    "{everyone}" to (mentions + author).toSmartString { "**${it.effectiveName(guild)}**" }
                )
            )
        }
    }

    override val helpHandler = Help(
        CommandDescription(names, commandName),
        Usage(CommandUsage(cmdName, description))
    )

    override fun toString() = "ActionCommand[cmd = $cmdName]"
}

class ImageCommandImpl(
    override val names: List<String>,
    override val category: Category?,
    override val commandName: String,
    override val description: String,
    override val provider: ImageProvider?,
    override val nsfwProvider: ImageProvider?,
    override val note: String?,
    private var messages: List<String>
) : ImageBasedCommandImpl() {
    override fun CommandContext.handle(image: Image) {
        channel.sendMessage {
            addFile(image.fileName, image.inputStream())
            content(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName(guild)}**") ?: "")
        }
    }

    override val helpHandler = Help(
        CommandDescription(names, commandName),
        Usage(CommandUsage(cmdName, description))
    )

    override fun toString() = "ImageCommand[cmd = $cmdName]"
}