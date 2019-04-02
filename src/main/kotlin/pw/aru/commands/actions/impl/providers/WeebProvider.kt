package pw.aru.commands.actions.impl.providers

import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import pw.aru.commands.actions.impl.Image
import pw.aru.commands.actions.impl.ImageProvider
import pw.aru.utils.URLCache

private typealias WeebImage = com.github.natanbc.weeb4j.image.Image
private typealias WeebImageProvider = com.github.natanbc.weeb4j.image.ImageProvider

class WeebProvider(
    private val provider: WeebImageProvider,
    private val cache: URLCache,
    private val type: String? = null,
    private val tags: List<String>? = null,
    private val fileType: FileType? = null,
    private val hiddenMode: HiddenMode = HiddenMode.DEFAULT,
    private val nsfwFilter: NsfwFilter = NsfwFilter.NO_NSFW
) : ImageProvider {

    private val WeebImage.fileName: String
        get() = "${type}_$id.${fileType.name.toLowerCase()}"

    override fun provide(): Image {
        val img = provider.getRandomImage(type, tags, hiddenMode, nsfwFilter, fileType).execute()

        return Image(img.fileName, img.url) {
            cache.cacheToFile(img.url).inputStream()
        }
    }
}