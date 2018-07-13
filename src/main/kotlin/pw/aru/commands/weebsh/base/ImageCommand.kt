package pw.aru.commands.weebsh.base

import com.github.natanbc.weeb4j.image.Image
import com.github.natanbc.weeb4j.image.ImageProvider
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.utils.caches.URLCache
import pw.aru.utils.extensions.randomOrNull
import pw.aru.utils.extensions.replaceEach

class ImageCommand(category: Category, imageProvider: ImageProvider, cache: URLCache, info: CommandInfo, img: GetImage, val msgs: List<String> = emptyList()) : WeebCommand(category, imageProvider, cache, info, img) {
    override fun onImage(event: GuildMessageReceivedEvent, image: Image) {
        val author = event.member
        event.channel
            .sendFile(cache.cacheToFile(image.url), image.name)
            .append(msgs.randomOrNull()?.replaceEach("{author}" to "**${author.effectiveName}**") ?: "")
            .queue()
    }

    private val Image.name: String
        get() = "${img.tags?.firstOrNull() ?: img.type}_$id.${fileType.name.toLowerCase()}"
}