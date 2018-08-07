package pw.aru.commands.actions.base

import pw.aru.api.nekos4j.image.Image
import pw.aru.api.nekos4j.image.ImageProvider
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.HelpDialogProvider
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.commands.help.*
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString

data class NekosLifeCommandInfo(
    val names: List<String>,
    val commandName: String,
    val description: String,
    val type: String,
    val nsfw: Boolean = false
) {
    val cmdName = names.first()
}

sealed class NekosLifeCommand(
    override val category: Category,
    private val provider: ImageProvider,
    registry: CommandRegistry,
    protected val cache: URLCache,
    protected val info: NekosLifeCommandInfo
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    abstract fun CommandContext.onImage(image: Image)

    override fun CommandContext.call() {
        if (info.nsfw && !requireNSFW()) return

        provider.getRandomImage(info.type).async {
            if (it == null) {
                send("$CONFUSED No images found... ").queue()
            } else {
                onImage(it)
            }
        }
    }

    protected val Image.name: String
        get() = url.substring(url.lastIndexOf("/") + 1)
}

class NekosLifeImageCommand(
    category: Category,
    provider: ImageProvider,
    registry: CommandRegistry,
    cache: URLCache,
    info: NekosLifeCommandInfo,
    private val messages: List<String> = emptyList()
) : NekosLifeCommand(category, provider, registry, cache, info) {
    override fun CommandContext.onImage(image: Image) {
        channel
            .sendFile(cache.cacheToFile(image.url), image.name)
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage(info.cmdName, info.description)
        ),
        Note("Powered by [Nekos.Life](https://nekos.life)")
    )
}

class NekosLifeActionCommand(
    category: Category,
    provider: ImageProvider,
    registry: CommandRegistry,
    cache: URLCache,
    info: NekosLifeCommandInfo,
    private val lines: ActionLines
) : NekosLifeCommand(category, provider, registry, cache, info) {
    override fun CommandContext.onImage(image: Image) {
        val mentions = event.message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> lines.noTargets
            mentions.all { it == event.message.member } -> lines.targetsYou
            mentions.all { it == event.guild.selfMember } -> lines.targetsMe
            else -> lines.anyTarget
        }

        channel
            .sendFile(cache.cacheToFile(image.url), image.url)
            .append(f.replaceEach("{author}" to "**${author.effectiveName}**", "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" }))
            .queue()
    }

    override val helpHandler = Help(
        CommandDescription(info.names, info.commandName),
        Usage(
            CommandUsage("${info.cmdName} [mentions]", info.description)
        ),
        Note("Powered by [Nekos.Life](https://nekos.life)")
    )
}