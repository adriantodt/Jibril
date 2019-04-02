package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.NsfwFilter.ONLY_NSFW
import okhttp3.OkHttpClient
import pw.aru.commands.actions.impl.ActionCommandsWorkshop
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache
import pw.aru.utils.text.CAT
import pw.aru.utils.text.DOG
import java.io.File

@CommandProvider
class ImageCommands(
    httpClient: OkHttpClient,
    weebApi: Weeb4J,
    private val assetProvider: ReloadableListProvider
) : ActionCommandsWorkshop(weebApi, URLCache(httpClient, File("url_cache")), Category.IMAGE) {
    override fun create() {
        imageCommand(listOf("cat"), "Cat Command", "Sends a random cat image.") {
            provider = fromWeebSh(type = "animal_cat")
            messages(
                "$CAT Aww, here, have a cat.",
                "$CAT {author}, are you sad? Have a cat!",
                "$CAT Meow.",
                "$CAT {author}, I think you need a cat."
            )
        }

        imageCommand(listOf("dog"), "Dog Command", "Sends a random dog image.") {
            provider = fromWeebSh(type = "animal_dog")
            messages(
                "$DOG Aww, here, have a dog.",
                "$DOG {author}, are you sad? Have a dog!",
                "$DOG Woof.",
                "$DOG {author}, I think you need a dog."
            )
        }

        imageCommand(listOf("catgirl", "neko"), "Catgirl Command", "Sends a random catgirl image.") {
            provider = fromWeebSh(type = "neko")
            nsfwProvider = fromWeebSh(type = "neko", nsfwFilter = ONLY_NSFW)
            messages(
                "$CAT Nyah~!"
            )
        }

        imageCommand(listOf("hybrid", "kemonomimi"), "Hybrid (Kemonomimi) Command", "Sends a random kemonomimi image.") {
            provider = fromWeebSh(type = "kemonomimi")
        }

        imageCommand(listOf("astolfo"), "Astolfo Command", "Sends a random Astolfo image.") {
            provider = fromWeebSh(tags = listOf("astolfo"))
        }

        imageCommand(listOf("wan"), "Wan Command", "Sends a random Wan image.") {
            provider = fromWeebSh(tags = listOf("wan"))
        }

        imageCommand(listOf("rem"), "Rem Command", "Sends a random Rem image.") {
            provider = fromWeebSh(type = "rem")
        }

        imageCommand(listOf("jibril"), "Jibril Command", "Sends a random Jibril image.") {
            provider = fromLinks(assetProvider["assets/aru/images/jibril.txt"])
        }

        imageCommand(listOf("megumin"), "Megumin Command", "Sends a random Megumin image.") {
            provider = fromWeebSh(type = "megumin")
        }

        imageCommand(listOf("trap"), "Trap Command", "Sends a random trap image.") {
            provider = fromWeebSh(type = "trap")
        }

        imageCommand(listOf("poi"), "Poi Command", "Sends a random Poi image.") {
            provider = fromWeebSh(type = "poi")
        }

        imageCommand(listOf("trapmemes"), "TrapMemes Command", "Sends a random trap meme.") {
            provider = fromWeebSh(tags = listOf("trap_memes"))
        }

        imageCommand(listOf("discordmemes"), "Discord Memes Command", "Sends a random discord meme.") {
            provider = fromWeebSh(type = "discord_memes")
        }

        imageCommand(listOf("jojo"), "JoJo Command", "Is that a fucking JoJo reference?") {
            provider = fromWeebSh(type = "jojo")
        }
    }
}