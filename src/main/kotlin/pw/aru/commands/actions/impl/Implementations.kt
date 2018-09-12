package pw.aru.commands.actions.impl

import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.CommandDescription
import pw.aru.core.commands.help.CommandUsage
import pw.aru.core.commands.help.Help
import pw.aru.core.commands.help.Usage
import pw.aru.utils.extensions.randomOf
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString

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
                if (!channel.isNSFW) sfw else if (args.takeString() == "nsfw") nsfw else randomOf(sfw, nsfw)
            }
            else -> throw RuntimeException("Impossible state")
        }

        val image = provider.provide()
        withMDC("command.cmdName" to cmdName, "command.uploadedImage" to image.meta) {
            handle(image)
        }
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
        val mentions = event.message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> noTargets
            mentions.all { it == event.message.member } -> targetsYou
            mentions.all { it == event.guild.selfMember } -> targetsMe
            else -> anyTarget
        }

        channel.sendFile(image.inputStream(), image.fileName)
            .append(f.replaceEach(
                "{author}" to "**${author.effectiveName}**",
                "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" },
                "{everyone}" to (mentions + author).toSmartString { "**${it.effectiveName}**" }
            ))
            .queue(null, exceptionHandler())
    }

    override val helpHandler = Help(
        CommandDescription(names, commandName),
        Usage(CommandUsage(cmdName, description))
    )

    override fun toString() = "ActionCommand[$cmdName]"
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
        channel.sendFile(image.inputStream(), image.fileName)
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue(null, exceptionHandler())
    }

    override val helpHandler = Help(
        CommandDescription(names, commandName),
        Usage(CommandUsage(cmdName, description))
    )

    override fun toString() = "ImageCommand[$cmdName]"
}