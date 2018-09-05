package pw.aru.commands.actions.v2

import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.HiddenMode.DEFAULT
import com.github.natanbc.weeb4j.image.NsfwFilter
import com.github.natanbc.weeb4j.image.NsfwFilter.NO_NSFW
import pw.aru.commands.actions.v2.providers.LocalFileProvider
import pw.aru.commands.actions.v2.providers.RandomURLProvider
import pw.aru.commands.actions.v2.providers.WeebProvider
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.ICommandProvider
import pw.aru.utils.caches.URLCache

abstract class ActionCommandsWorkshop(weebApi: Weeb4J, private val cache: URLCache, private val category: Category) : ICommandProvider {
    //You only need to implement this
    abstract fun create()

    //ICommandProvider implementation
    private lateinit var registry: CommandRegistry

    override fun provide(r: CommandRegistry) {
        registry = r
        create()
    }

    //Builders
    protected fun actionCommand(names: List<String>, commandName: String, description: String, block: ActionCommandBuilder.() -> Unit) {
        registry.register(
            names.toTypedArray(),
            ActionCommandBuilder(names, category, commandName, description)
                .apply(block)
                .build()
        )
    }

    protected fun imageCommand(names: List<String>, commandName: String, description: String, block: ImageCommandBuilder.() -> Unit) {
        registry.register(
            names.toTypedArray(),
            ImageCommandBuilder(names, category, commandName, description)
                .apply(block)
                .build()
        )
    }

    // Utility Functions
    private val weebProvider by lazy(weebApi::getImageProvider)

    protected fun fromWeebSh(
        type: String? = null,
        tags: List<String>? = null,
        fileType: FileType? = null,
        hiddenMode: HiddenMode = DEFAULT,
        nsfwFilter: NsfwFilter = NO_NSFW
    ) = WeebProvider(weebProvider, cache, type, tags, fileType, hiddenMode, nsfwFilter)

    protected fun fromLocalFiles(vararg fileList: String) = fromLocalFiles(fileList.toList())
    protected fun fromLocalFiles(files: List<String>) = LocalFileProvider(files)

    protected fun fromLinks(vararg fileList: String) = fromLinks(fileList.toList())
    protected fun fromLinks(files: List<String>) = RandomURLProvider(cache, files)

    //Private Functions
    private fun ActionCommandBuilder.build() = ActionCommandImpl(names, category, commandName, description, provider, nsfwProvider, note, anyTarget, noTargets, targetsYou, targetsMe)

    private fun ImageCommandBuilder.build() = ImageCommandImpl(names, category, commandName, description, provider, nsfwProvider, note, messages)
}