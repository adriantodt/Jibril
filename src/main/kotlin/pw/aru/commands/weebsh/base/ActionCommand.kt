package pw.aru.commands.weebsh.base

import com.github.natanbc.weeb4j.image.Image
import com.github.natanbc.weeb4j.image.ImageProvider
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.utils.caches.URLCache
import pw.aru.utils.extensions.replaceEach
import pw.aru.utils.extensions.toSmartString

data class ActionLines(
    val anyTarget: String,
    val noTargets: String,
    val targetsYou: String,
    val targetsMe: String
)

class ActionCommand(category: Category, imageProvider: ImageProvider, cache: URLCache, info: CommandInfo, img: GetImage, private val lines: ActionLines) : WeebCommand(category, imageProvider, cache, info, img) {

    override fun onImage(event: GuildMessageReceivedEvent, image: Image) {
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
            .addFile(cache.cacheToFile(image.url), image.name)
            .queue()
    }

    private val Image.name: String
        get() = "${img.tags?.firstOrNull() ?: img.type}_$id.${fileType.name.toLowerCase()}"
}