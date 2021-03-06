package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import pw.aru.bot.categories.Category
import pw.aru.bot.commands.CommandProvider
import pw.aru.commands.actions.impl.ActionCommandsWorkshop
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache

@CommandProvider
class NSFWImageCommands(
    weebApi: Weeb4J,
    cache: URLCache,
    private val assetProvider: ReloadableListProvider
) : ActionCommandsWorkshop(weebApi, cache, Category.NSFW_IMAGE) {
    override fun create() {

    }
}