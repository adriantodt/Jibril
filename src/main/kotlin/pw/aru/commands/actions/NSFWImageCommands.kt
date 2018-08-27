package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import okhttp3.OkHttpClient
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.caches.URLCache
import java.io.File

@CommandProvider
class NSFWImageCommands(
    httpClient: OkHttpClient,
    weebApi: Weeb4J,
    private val assetProvider: ReloadableListProvider
) : ICommandProvider {
    private val weebProvider = weebApi.imageProvider
    private val cache = URLCache(httpClient, File("url_cache"))

    override fun provide(r: CommandRegistry) {
        val category = Category.NSFW_IMAGE

    }
}