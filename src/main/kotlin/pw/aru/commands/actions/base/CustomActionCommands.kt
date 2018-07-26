package pw.aru.commands.actions.base

import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.HelpDialogProvider
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.caches.URLCache
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.extensions.random
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString

data class CustomCommandInfo(
    val names: List<String>,
    val name: String,
    val fileName: String,
    val description: String,
    val nsfw: Boolean = false
)

class CustomImageCommand(
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

        val author = event.member
        event.channel
            .sendFile(cache.cacheToFile(images.random()), info.fileName)
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    override val helpHandler = HelpFactory(info.name) {
        val name = info.names.first()
        aliases(*info.names.drop(1).toTypedArray())

        usage(name, info.description)
    }
}

class CustomActionCommand(
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

        val author = event.member
        val mentions = event.message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> lines.noTargets
            mentions.all { it == event.message.author } -> lines.targetsYou
            mentions.all { it == event.guild.selfMember } -> lines.targetsMe
            else -> lines.anyTarget
        }

        event.channel
            .sendMessage(f.replaceEach("{author}" to "**${author.effectiveName}**", "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" }))
            .addFile(cache.cacheToFile(images.random()), info.fileName)
            .queue()
    }

    override val helpHandler = HelpFactory(info.name) {
        val name = info.names.first()
        aliases(*info.names.drop(1).toTypedArray())

        usage("$name [mentions]", info.description)
    }

}