package pw.aru.commands.actions.impl.providers

import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import pw.aru.commands.actions.impl.Image
import pw.aru.commands.actions.impl.ImageProvider
import pw.aru.utils.caches.URLCache

private typealias WeebImage = com.github.natanbc.weeb4j.image.Image
private typealias WeebImageProvider = com.github.natanbc.weeb4j.image.ImageProvider

class WeebProvider(
    private val provider: WeebImageProvider,
    private val cache: URLCache,
    val type: String? = null,
    val tags: List<String>? = null,
    val fileType: FileType? = null,
    val hiddenMode: HiddenMode = HiddenMode.DEFAULT,
    val nsfwFilter: NsfwFilter = NsfwFilter.NO_NSFW
) : ImageProvider {

    private val WeebImage.fileName: String
        get() = "${type}_$id.${fileType.name.toLowerCase()}"

    override fun provide(): Image {
        val img = provider.getRandomImage(type, tags, hiddenMode, nsfwFilter, fileType).execute()

        return Image(img.fileName) {
            cache.cacheToFile(img.url).inputStream()
        }
    }
}