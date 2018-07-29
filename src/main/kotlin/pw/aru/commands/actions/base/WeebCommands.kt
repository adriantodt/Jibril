package pw.aru.commands.actions.base

import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.Image
import com.github.natanbc.weeb4j.image.ImageProvider
import com.github.natanbc.weeb4j.image.NsfwFilter.*
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommand
import pw.aru.core.commands.ICommand.HelpDialogProvider
import pw.aru.core.commands.context.CommandContext
import pw.aru.utils.caches.URLCache
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.CONFUSED
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString

data class WeebCommandInfo(
    val names: List<String>,
    val name: String,
    val description: String
)

data class GetImage(
    val type: String? = null,
    val tags: List<String>? = null,
    val fileType: FileType? = null
)

sealed class WeebCommand(
    override val category: Category,
    private val provider: ImageProvider,
    registry: CommandRegistry,
    protected val cache: URLCache,
    protected val info: WeebCommandInfo,
    private val img: GetImage
) : ICommand, HelpDialogProvider {

    init {
        @Suppress("LeakingThis")
        registry.register(info.names.toTypedArray(), this)
    }

    abstract fun CommandContext.onImage(image: Image)

    override fun CommandContext.call() {
        val args = parseable()

        val nsfw = if (channel.isNSFW) if (args.takeString() == "nsfw") ONLY_NSFW else ALLOW_NSFW else NO_NSFW

        provider.getRandomImage(img.type, img.tags, null, nsfw, img.fileType).async {
            if (it == null) {
                send("$CONFUSED No images found... ").queue()
            } else {
                onImage(it)
            }
        }
    }

    protected val Image.name: String
        get() = "${img.tags?.firstOrNull() ?: img.type}_$id.${fileType.name.toLowerCase()}"
}

class WeebImageCommand(
    category: Category,
    provider: ImageProvider,
    registry: CommandRegistry,
    cache: URLCache,
    info: WeebCommandInfo,
    img: GetImage,
    private val messages: List<String> = emptyList()
) : WeebCommand(category, provider, registry, cache, info, img) {
    override fun CommandContext.onImage(image: Image) {
        channel
            .sendFile(cache.cacheToFile(image.url), image.name)
            .append(messages.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    override val helpHandler = HelpFactory(info.name) {
        val name = info.names.first()
        aliases(*info.names.drop(1).toTypedArray())

        usage(name, info.description)
        usage("$name nsfw", "Only NSFW images. Might not find an image depending on the command.")

        note("Powered by https://weeb.sh/")
    }
}

class WeebActionCommand(
    category: Category,
    provider: ImageProvider,
    registry: CommandRegistry,
    cache: URLCache,
    info: WeebCommandInfo,
    img: GetImage,
    private val lines: ActionLines
) : WeebCommand(category, provider, registry, cache, info, img) {
    override fun CommandContext.onImage(image: Image) {
        val mentions = event.message.mentionedMembers

        val f = when {
            mentions.isEmpty() -> lines.noTargets
            mentions.all { it == event.message.member } -> lines.targetsYou
            mentions.all { it == event.guild.selfMember } -> lines.targetsMe
            else -> lines.anyTarget
        }

        channel
            .sendFile(cache.cacheToFile(image.url), image.name)
            .append(f.replaceEach("{author}" to "**${author.effectiveName}**", "{mentions}" to mentions.toSmartString { "**${it.effectiveName}**" }))
            .queue()
    }

    override val helpHandler = HelpFactory(info.name) {
        val name = info.names.first()
        aliases(*info.names.drop(1).toTypedArray())

        usage("$name [mentions]", info.description)
        usage("$name nsfw [mentions]", "Only NSFW images. Might not find an image depending on the command.")

        note("Powered by https://weeb.sh/")
    }
}