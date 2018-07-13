package pw.aru.commands.weebsh.base

import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.Image
import com.github.natanbc.weeb4j.image.ImageProvider
import com.github.natanbc.weeb4j.image.NsfwFilter.*
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.categories.Category
import pw.aru.core.commands.ArgsCommand
import pw.aru.core.commands.ICommand.HelpDialogProvider
import pw.aru.core.parser.Args
import pw.aru.utils.caches.URLCache
import pw.aru.utils.commands.HelpFactory
import pw.aru.utils.emotes.CONFUSED

data class CommandInfo(
    val name: String,
    val description: String,
    val aliases: Array<String>? = null
)

data class GetImage(
    val type: String? = null,
    val tags: List<String>? = null,
    val fileType: FileType? = null
)

abstract class WeebCommand(
    override val category: Category,
    private val provider: ImageProvider,
    protected val cache: URLCache,
    protected val info: CommandInfo,
    protected val img: GetImage
) : ArgsCommand(), HelpDialogProvider {

    abstract fun onImage(event: GuildMessageReceivedEvent, image: Image)

    override fun call(event: GuildMessageReceivedEvent, args: Args) {
        val nsfw = if (event.channel.isNSFW) if (args.takeString() == "nsfw") ONLY_NSFW else ALLOW_NSFW else NO_NSFW

        provider.getRandomImage(img.type, img.tags, null, nsfw, img.fileType).async {
            if (it == null) {
                event.channel.sendMessage("$CONFUSED No images found... ").queue()
            } else {
                onImage(event, it)
            }
        }
    }

    override val helpHandler = HelpFactory(info.name) {
        if (info.aliases != null) aliases(*info.aliases)
        description(info.description)
        note("Powered by https://weeb.sh/")
    }

}