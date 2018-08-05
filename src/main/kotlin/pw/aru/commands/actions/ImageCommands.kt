package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import okhttp3.OkHttpClient
import pw.aru.commands.actions.base.*
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Categories
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.CAT
import pw.aru.utils.emotes.DOG
import java.io.File

@CommandProvider
class ImageCommands(httpClient: OkHttpClient, weebApi: Weeb4J) : ICommandProvider {
    private val provider = weebApi.imageProvider
    private val cache = URLCache(httpClient, File("url_cache"))

    override fun provide(r: CommandRegistry) {
        val category = Categories.IMAGE

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("cat"), "Cat Command", "Sends a random cat image."),
            GetImage(type = "animal_cat"),
            listOf(
                "$CAT Aww, here, have a cat.",
                "$CAT {author}, are you sad? Have a cat!",
                "$CAT Meow.",
                "$CAT {author}, I think you need a cat."
            )
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("dog"), "Dog Command", "Sends a random dog image."),
            GetImage(type = "animal_dog"),
            listOf(
                "$DOG Aww, here, have a dog.",
                "$DOG {author}, are you sad? Have a dog!",
                "$DOG Woof.",
                "$DOG {author}, I think you need a dog."
            )
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("catgirl", "neko"), "Catgirl Command", "Sends a random catgirl image."),
            GetImage(type = "neko"),
            listOf(
                "$CAT Nyah~!"
            )
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("hybrid", "kemonomimi"), "Hybrid (Kemonomimi) Command", "Sends a random kemonomimi image."),
            GetImage(type = "kemonomimi")
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("astolfo"), "Astolfo Command", "Sends a random Astolfo image."),
            GetImage(tags = listOf("astolfo"))
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("wan"), "Wan Command", "Sends a random Wan image."),
            GetImage(tags = listOf("wan"))
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("rem"), "Rem Command", "Sends a random Rem image."),
            GetImage(type = "rem")
        )

        CustomImageCommand(
            category, r, cache,
            CustomCommandInfo(listOf("jibril"), "Jibril Command", "jibril.gif", "Sends a random Jibril image."),
            File("assets/aru/images/jibril.txt").readLines()
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("megumin"), "Megumin Command", "Sends a random Megumin image."),
            GetImage(type = "megumin")
        )


        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("trap"), "Trap Command", "Sends a random trap image."),
            GetImage(type = "trap")
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("poi"), "Poi Command", "Sends a random Poi image."),
            GetImage(type = "poi")
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("trapmemes"), "TrapMemes Command", "Sends a random trap meme."),
            GetImage(tags = listOf("trap_memes"))
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("discordmemes"), "Discord Memes Command", "Sends a random discord meme."),
            GetImage(type = "discord_memes")
        )

        WeebImageCommand(
            category, provider, r, cache,
            WeebCommandInfo(listOf("jojo"), "JoJo Command", "Is that a fucking JoJo reference?"),
            GetImage(type = "jojo")
        )
    }

}