package pw.aru.commands.weebsh

import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType.GIF
import okhttp3.OkHttpClient
import pw.aru.commands.weebsh.base.*
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Categories
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.exported.aru_version
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.*
import java.io.File

@CommandProvider
class WeebCommands(httpClient: OkHttpClient, weebApi: Weeb4J) : ICommandProvider {
    private val provider = weebApi.imageProvider
    private val cache = URLCache(httpClient, File("url_cache"))

    /*
    types: [
        bang, dab, dance, delet_this, deredere, greet,
        handholding, highfive, insult, megumin, nani, poi,
        poke, pout, punch, rem, slap, sleepy, smile, sumfuk,
        teehee, tickle, trap, thumbsup, triggered, wag, waifu_insult, wasted
    ]
     */

    private fun actionCommands(r: CommandRegistry) {
        val category = Categories.ACTION

        r["pat"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Pat Command", "Pats the specified user."),
            GetImage(type = "pat", fileType = GIF),
            ActionLines(
                "$PAT {mentions}, you have been patted by {author}",
                "$PAT *Pats~*",
                "$PAT *Pats you~*",
                "$PAT Oh, eh.. *~gets patted~* T-thanks~"
            )
        )

        r["cuddle"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Cuddle Command", "Cuddles the specified user."),
            GetImage(type = "cuddle", fileType = GIF),
            ActionLines(
                "$CUDDLE {mentions}, you have been cuddled by {author}",
                "$CUDDLE *Cuddles~*",
                "$CUDDLE *Cuddles you~*",
                "$CUDDLE Oh, eh.. *~gets cuddled~* T-thanks~"
            )
        )

        r["hug"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Hug Command", "Hugs the specified user."),
            GetImage(type = "kiss", fileType = GIF),
            ActionLines(
                "$HUG {mentions}, you have been hugged by {author}",
                "$HUG *Hugs~*",
                "$HUG *Hugs you~*",
                "$HUG Oh, eh.. *~gets hugged~* T-thanks~"
            )
        )

        r["kiss"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Kiss Command", "Kisses the specified user."),
            GetImage(type = "kiss", fileType = GIF),
            ActionLines(
                "$KISS {mentions}, you have been kissed by {author}",
                "$KISS *Kisses~*",
                "$KISS *Kisses you~*",
                "$KISS Oh, eh.. *~gets kissed~* T-thanks~"
            )
        )

        r["lick"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Lick Command", "Licks the specified user."),
            GetImage(type = "lick", fileType = GIF),
            ActionLines(
                "$LICK {mentions}, {author} is staring you",
                "$LICK *Licks~*",
                "$LICK *Licks you~*",
                "$LICK Eeh..? T-that's awkward, stop!"
            )
        )

        r["bite"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Bite Command", "Bite the specified user."),
            GetImage(type = "bite", fileType = GIF),
            ActionLines(
                "$BITE {mentions}, you have been bitten by {author}",
                "$BITE *Bites~*",
                "$BITE *Bites you~*",
                "$BITE O-Ouch! That hurts, you baka!"
            )
        )

        r["stare"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Stare Command", "Stares the specified user."),
            GetImage(type = "stare", fileType = GIF),
            ActionLines(
                "$STARE {mentions}, {author} is staring you",
                "$STARE *Stares~*",
                "$STARE *Stares you~*",
                "$STARE Eeh..? T-that's awkward, stop!"
            )
        )

        r["cry"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Cry Command", "It's okay to cry."),
            GetImage(type = "cry", fileType = GIF),
            ActionLines(
                "$CRY {mentions}, {author} is crying",
                "$CRY *Cries~*",
                "$CRY *Cries~*",
                "$CRY Eeh..? D-Did I do something wrong? I-I'm sorry!"
            )
        )

        r["smug"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Smug Command", "It's never late enough to be smug."),
            GetImage(type = "smug", fileType = GIF),
            ActionLines(
                "$SMUG {mentions}, {author} is looking a bit smug",
                "$SMUG {author} is looking a bit smug",
                "$SMUG {author} is looking a bit smug",
                "$SMUG Eeh..? T-that's awkward, stop!"
            )
        )

        r["blush"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Blush Command", "When it's just too much to handle."),
            GetImage(type = "banghead", fileType = GIF),
            listOf("$BLUSH {author} is slowly turning into a tomato")
        )

        r["facedesk", "banghead"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Facedesk Command", "When it's just too much to handle."),
            GetImage(type = "banghead", fileType = GIF),
            listOf("$TALKING *Facedesks~*")
        )

        r["drift"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Drift Command", "*Initial D intensifies.*"),
            GetImage(type = "initial_d", fileType = GIF),
            listOf("$CAR *Drifts in japanese~*")
        )

        r["thinking"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Thinking Command", "Sends a random thinking image."),
            GetImage(type = "thinking")
        )


        r["nom"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Nom Command", "Sends a random nom image."),
            GetImage(type = "nom")
        )

        r["lewd"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Lewd Command", "T-that's lewd!"),
            GetImage(type = "lewd"),
            ActionLines(
                "$LEWD {mentions}, Y-you lewdie!",
                "$LEWD Y-you lewdie!",
                "$LEWD Y-you lewdie!",
                "$LEWD I-I'm not lewd, you lewdie!"
            )
        )

        r["owo"] = ImageCommand(
            category, provider, cache,
            CommandInfo("OwO Command", "What's this?"),
            GetImage(type = "owo")
        )

        r["shrug"] = ActionCommand(
            category, provider, cache,
            CommandInfo("Shrug Command", "Sends a random shrug image."),
            GetImage(type = "shrug", fileType = GIF),
            ActionLines(
                "$SHRUG {mentions}, {author} is shrugging at you",
                "$SHRUG {author} is shrugging",
                "$SHRUG {author} is shrugging",
                "$SHRUG W-Why are you Shrugging at me?"
            )
        )

        r["awoo", "awo", "awooo", "awoooo"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Awoo Command", "Sends a random awoo!", arrayOf("awo", "awooo", "awoooo")),
            GetImage(type = "awoo", fileType = GIF),
            listOf(
                "$AWOO Awooo~!"
            )
        )

    }

    private fun imageCommands(r: CommandRegistry) {
        val category = Categories.IMAGE

        r["cat"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Cat Command", "Sends a random cat image."),
            GetImage(type = "animal_cat"),
            listOf(
                "$CAT Aww, here, have a cat.",
                "$CAT {author}, are you sad? Have a cat!",
                "$CAT Meow.",
                "$CAT {author}, I think you need a cat."
            )
        )

        r["dog"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Dog Command", "Sends a random dog image."),
            GetImage(type = "animal_dog"),
            listOf(
                "$DOG Aww, here, have a dog.",
                "$DOG {author}, are you sad? Have a dog!",
                "$DOG Woof.",
                "$DOG {author}, I think you need a dog."
            )
        )

        r["catgirl", "neko"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Catgirl Command", "Sends a random catgirl image.", arrayOf("neko")),
            GetImage(type = "neko"),
            listOf(
                "$CAT Nyah~!"
            )
        )

        r["hybrid", "kemonomimi"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Hybrid Girls Command", "Sends a random kemonomimi image.", arrayOf("kemonomimi")),
            GetImage(type = "kemonomimi")
        )

        r["astolfo"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Astolfo Command", "Sends a random Astolfo image."),
            GetImage(tags = listOf("astolfo"))
        )

        r["wan"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Wan Command", "Sends a random Wan image."),
            GetImage(tags = listOf("wan"))
        )

        r["trapmemes"] = ImageCommand(
            category,
            provider, cache,
            CommandInfo("TrapMemes Command", "Sends a random trap meme."),
            GetImage(tags = listOf("trap_memes"))
        )

        r["discordmemes"] = ImageCommand(
            category, provider, cache,
            CommandInfo("Discord Memes Command", "Sends a random discord meme."),
            GetImage(type = "discord_memes")
        )

        r["jojo"] = ImageCommand(
            category, provider, cache,
            CommandInfo("JoJo Command", "Is that a fucking JoJo reference?"),
            GetImage(type = "jojo")
        )
    }

    override fun provide(r: CommandRegistry) {
        actionCommands(r)
        imageCommands(r)
    }
}

fun main(args: Array<String>) {
    val api = Weeb4J.Builder()
        .setToken(TokenType.WOLKE, "U0oxOENvWFE3OmVlNjQwMDY2MzEzMzc0NDllY2VlY2U2OGRkOTQ4OTVjNTk5NTRmNDQwNWI1NDEzOTFiNDJlOGJj")
        .setBotInfo("AruDev!", aru_version, "development")
        .build()

    with(api.imageProvider) {
        imageTags.async {
            println("tags: $it (${it.size})")
        }
        imageTypes.async {
            println("types: ${it.types} (${it.types.size})")
            println("previewImages: ${it.previewImages} (${it.previewImages.size})")
        }
    }
}