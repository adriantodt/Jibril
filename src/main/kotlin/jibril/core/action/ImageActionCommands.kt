package jibril.core.action

import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import com.github.natanbc.weeb4j.image.NsfwFilter.ALLOW_NSFW
import com.github.natanbc.weeb4j.image.NsfwFilter.NO_NSFW
import jibril.core.categories.Categories
import jibril.core.categories.Category
import jibril.core.commands.ICommand
import jibril.utils.caches.URLCache
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

//Base Classes
data class Image(val id: String, val content: ByteArray)

sealed class ImageActionCommand : ICommand {
    abstract fun randomImage(nsfwFilter: NsfwFilter): Image
    abstract fun format(event: GuildMessageReceivedEvent): String?

    abstract val imageName: String

    override val category: Category = Categories.ACTION

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        val (id, content) = randomImage(if (event.channel.isNSFW) ALLOW_NSFW else NO_NSFW)
        event.channel.sendFile(content, "$imageName-$id.png")
            .content(format(event))
            .queue()
    }
}

sealed class WeebShImageActionCommand(private val weebApi: Weeb4J) : ImageActionCommand() {
    val cache = URLCache(50)

    abstract val imageType: String

    override fun randomImage(nsfwFilter: NsfwFilter): Image {
        val image = weebApi.getRandomImage(imageType, listOf(imageType), HiddenMode.DEFAULT, nsfwFilter, FileType.GIF).execute()!!
        return Image(image.id, cache.getFile(image.url).readBytes())
    }
}