package pw.aru.commands.actions.base

import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.HelpDialogProvider
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.caches.URLCache
import pw.aru.utils.extensions.random
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString
import java.io.File

data class CustomCommandInfo(
    val names: List<String>,
    val commandName: String,
    val description: String,
    val nsfw: Boolean = false
) {
    val cmdName = names.first()
}

class URLsImageCommand(
    override val category: Category,
    registry: CommandRegistry,
    private val cache: URLCache,
    private val info: CustomCommandInfo,
    private val images: List<String>,
    private val messages: List<String> = emptyList()
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    override fun CommandContext.call() {
        if (info.nsfw && !requireNSFW()) return

        val url = images.random()
        channel
            .sendFile(cache.cacheToFile(url), url.substring(url.lastIndexOf('/') + 1))
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage(info.cmdName, info.description)
        ),
        Note("This command is an exclusive of Aru! You can suggest more images [joining our support server!](https://support.aru.site)")
    )
}

class URLsActionCommand(
    override val category: Category,
    registry: CommandRegistry,
    private val cache: URLCache,
    private val info: CustomCommandInfo,
    private val images: List<String>,
    private val lines: ActionLines
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    override fun CommandContext.call() {
        if (info.nsfw && !requireNSFW()) return

        val mentions = message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> lines.noTargets
            mentions.all { it == event.message.member } -> lines.targetsYou
            mentions.all { it == event.guild.selfMember } -> lines.targetsMe
            else -> lines.anyTarget
        }

        val url = images.random()
        channel
            .sendFile(cache.cacheToFile(url), url.substring(url.lastIndexOf('/') + 1))
            .append(f.replaceEach("{author}" to "**${author.effectiveName}**", "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" }))
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage("${info.cmdName} [mentions]", info.description)
        ),
        Note("This command is an exclusive of Aru! You can suggest more images [joining our support server!](https://support.aru.site)")
    )
}

class LocalImageCommand(
    override val category: Category,
    registry: CommandRegistry,
    private val info: CustomCommandInfo,
    private val images: List<String>,
    private val messages: List<String> = emptyList()
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    override fun CommandContext.call() {
        if (info.nsfw && !requireNSFW()) return

        channel
            .sendFile(File(images.random()))
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage(info.cmdName, info.description)
        ),
        Note("This command is an exclusive of Aru! You can suggest more images [joining our support server!](https://support.aru.site)")
    )
}

class LocalActionCommand(
    override val category: Category,
    registry: CommandRegistry,
    private val cache: URLCache,
    private val info: CustomCommandInfo,
    private val images: List<String>,
    private val lines: ActionLines
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    override fun CommandContext.call() {
        if (info.nsfw && !requireNSFW()) return

        val mentions = message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> lines.noTargets
            mentions.all { it == event.message.member } -> lines.targetsYou
            mentions.all { it == event.guild.selfMember } -> lines.targetsMe
            else -> lines.anyTarget
        }

        channel
            .sendFile(File(images.random()))
            .append(f.replaceEach("{author}" to "**${author.effectiveName}**", "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" }))
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage("${info.cmdName} [mentions]", info.description)
        ),
        Note("This command is an exclusive of Aru! You can suggest more images [joining our support server!](https://support.aru.site)")
    )
}