package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import pw.aru.commands.actions.impl.ActionCommandsWorkshop
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache

@CommandProvider
class NSFWImageCommands(
    cache: URLCache,
    weebApi: Weeb4J,
    private val assetProvider: ReloadableListProvider
) : ActionCommandsWorkshop(weebApi, cache, Category.NSFW_IMAGE) {
    override fun create() {

    }
}