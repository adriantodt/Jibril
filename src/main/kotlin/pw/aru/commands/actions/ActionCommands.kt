package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType.GIF
import okhttp3.OkHttpClient
import pw.aru.api.nekos4j.Nekos4J
import pw.aru.commands.actions.base.*
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.*
import java.io.File

@CommandProvider
class ActionCommands(
    httpClient: OkHttpClient,
    weebApi: Weeb4J,
    nekoApi: Nekos4J,
    private val assetProvider: ReloadableListProvider
) : ICommandProvider {

    private val weebProvider = weebApi.imageProvider
    private val nekoProvider = nekoApi.imageProvider
    private val cache = URLCache(httpClient, File("url_cache"))

    /*
    weeb4j: {
        types: [
            deredere, greet, insult, sleepy,
            sumfuk, triggered, waifu_insult, wasted
        ],
        types.rejected: [
            bang,       // Reason: Not good enough to do a "shoot" command, and might break Discord ToS
            delet_this, // Reason: Not much sense at all neither funny
            nani        // Reason: Only Hotoku no Ken memes, not funny tbh
        ]
    }
     */

    override fun provide(r: CommandRegistry) {
        val category = Category.ACTION

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("pat"), "Pat Command", "Pats the mentioned users."),
            GetImage(type = "pat", fileType = GIF),
            ActionLines(
                "$PAT {mentions}, you have been patted by {author}",
                "$PAT *Pats~*",
                "$PAT *Pats you~*",
                "$PAT Oh, eh.. *~gets patted~* T-thanks~"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("poke"), "Poke Command", "Pokes the mentioned users."),
            GetImage(type = "poke", fileType = GIF),
            ActionLines(
                "$POKE {mentions}, {author} is poking you",
                "$POKE *Pokes~*",
                "$POKE *Pokes you~*",
                "$POKE Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("highfive"), "Highfive Command", "Highfives the mentioned users."),
            GetImage(type = "highfive", fileType = GIF),
            ActionLines(
                "$HIGHFIVE {author} is high-fiving {mentions}",
                "$HIGHFIVE *Highfives~*",
                "$HIGHFIVE *Highfives you~*",
                "$HIGHFIVE Oh, eh.. *~high-fives back~* Hai!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("tease", "teehee"), "Tease Command", "Teases the mentioned users."),
            GetImage(type = "teehee", fileType = GIF),
            ActionLines(
                "$TEEHEE {mentions}, {author} is teasing you",
                "$TEEHEE *Teases~*",
                "$TEEHEE *Teases you~*",
                "$TEEHEE Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("tickle"), "Tickle Command", "Tickles the mentioned users."),
            GetImage(type = "teehee", fileType = GIF),
            ActionLines(
                "$TEEHEE {mentions}, {author} is tickling you",
                "$TEEHEE *Tickles~*",
                "$TEEHEE *Tickles you~*",
                "$TEEHEE Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("slap"), "Slap Command", "Slaps the mentioned users."),
            GetImage(type = "slap", fileType = GIF),
            ActionLines(
                "$EVIL {mentions}, you have been slapped by {author}",
                "$EVIL *Slaps~*",
                "$EVIL *Slaps you~*",
                "$EVIL Eeh..? *~gets slapped~* Y-you BAKA! $CRY"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("smile"), "Smile Command", "Smiles at the mentioned users."),
            GetImage(type = "smile", fileType = GIF),
            ActionLines(
                "$SMILE {mentions}, {author} is smiling at you",
                "$SMILE *Smiles~*",
                "$SMILE *Smiles at you~*",
                "$SMILE Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("pout"), "Pout Command", "Pouts at the mentioned users."),
            GetImage(type = "pout", fileType = GIF),
            ActionLines(
                "$POUT {mentions}, {author} is pouting at you",
                "$POUT *Pouts~*",
                "$POUT *Pouts at you~*",
                "$POUT Eeh..? W-what do you want?"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("cuddle"), "Cuddle Command", "Cuddles the mentioned users."),
            GetImage(type = "cuddle", fileType = GIF),
            ActionLines(
                "$CUDDLE {mentions}, you have been cuddled by {author}",
                "$CUDDLE *Cuddles~*",
                "$CUDDLE *Cuddles you~*",
                "$CUDDLE Oh, eh.. *~gets cuddled~* T-thanks~"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("punch"), "Punch Command", "Punches the mentioned users."),
            GetImage(type = "punch", fileType = GIF),
            ActionLines(
                "$PUNCH {mentions}, you have been punched by {author}",
                "$PUNCH *Punches~*",
                "$PUNCH *Punches you~*",
                "$PUNCH Eeh..? *~gets punched~* Y-you BAKA! $CRY"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("holdhands", "holdhand"), "Hold Hands Command", "Holds the hands of the mentioned users."),
            GetImage(type = "handholding", fileType = GIF),
            ActionLines(
                "$CUDDLE {author} is holding {mentions}'s hand",
                "$CUDDLE *Holds hands~*",
                "$CUDDLE *Holds your hand~*",
                "$CUDDLE Oh, eh.. T-thanks~ $BLUSH"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("hug"), "Hug Command", "Hugs the mentioned users."),
            GetImage(type = "hug", fileType = GIF),
            ActionLines(
                "$HUG {mentions}, you have been hugged by {author}",
                "$HUG *Hugs~*",
                "$HUG *Hugs you~*",
                "$HUG Oh, eh.. *~gets hugged~* T-thanks~"
            )
        )

        CustomActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("nuzzle"), "Nuzzle Command", "Nuzzles the mentioned users.", "nuzzle.gif"),
            assetProvider["assets/aru/actions/nuzzle.txt"],
            ActionLines(
                "$CUDDLE {mentions}, you have been nuzzled by {author}",
                "$CUDDLE *Nuzzles~*",
                "$CUDDLE *Nuzzles you~*",
                "$CUDDLE Oh, eh.. *~gets nuzzled~* T-thanks~"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("kiss"), "Kiss Command", "Kisses the mentioned users."),
            GetImage(type = "kiss", fileType = GIF),
            ActionLines(
                "$KISS {mentions}, you have been kissed by {author}",
                "$KISS *Kisses~*",
                "$KISS *Kisses you~*",
                "$KISS Oh, eh.. *~gets kissed~* T-thanks~"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("lick"), "Lick Command", "Licks the mentioned users."),
            GetImage(type = "lick", fileType = GIF),
            ActionLines(
                "$LICK {mentions}, {author} is licking you",
                "$LICK *Licks~*",
                "$LICK *Licks you~*",
                "$LICK Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("bite"), "Bite Command", "Bite the mentioned users."),
            GetImage(type = "bite", fileType = GIF),
            ActionLines(
                "$BITE {mentions}, you have been bitten by {author}",
                "$BITE *Bites~*",
                "$BITE *Bites you~*",
                "$BITE O-Ouch! That hurts, you baka!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("stare"), "Stare Command", "Stares the mentioned users."),
            GetImage(type = "stare", fileType = GIF),
            ActionLines(
                "$STARE {mentions}, {author} is staring you",
                "$STARE *Stares~*",
                "$STARE *Stares you~*",
                "$STARE Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("cry"), "Cry Command", "It's okay to cry."),
            GetImage(type = "cry", fileType = GIF),
            ActionLines(
                "$CRY {mentions}, {author} is crying",
                "$CRY *Cries~*",
                "$CRY *Cries~*",
                "$CRY Eeh..? D-Did I do something wrong? I-I'm sorry!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("smug"), "Smug Command", "It's never late enough to be smug."),
            GetImage(type = "smug", fileType = GIF),
            ActionLines(
                "$SMUG {mentions}, {author} is looking a bit smug",
                "$SMUG {author} is looking a bit smug",
                "$SMUG {author} is looking a bit smug",
                "$SMUG Eeh..? T-that's awkward, stop!"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("wag", "tail", "tailwag", "wagtail"), "Wag Tail Command", "Wags tails at the mentioned users."),
            GetImage(type = "wag", fileType = GIF),
            ActionLines(
                "$AWOO {mentions}, {author} is wagging the tail at you!",
                "$AWOO *Wags tail~*",
                "$AWOO *Wags tail at you~*",
                "$AWOO Eeh..? W-what do you want?"
            )
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("blush"), "Blush Command", "When it's just too much for you to handle."),
            GetImage(type = "banghead", fileType = GIF),
            listOf("$BLUSH {author} is slowly turning into a tomato")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("dance"), "Dance Command", "Sends a random dance image."),
            GetImage(type = "dance", fileType = GIF),
            listOf("$DANCE {author} is dancing $DANCE2")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("facedesk", "banghead"), "Facedesk Command", "When it's just too much to handle."),
            GetImage(type = "banghead", fileType = GIF),
            listOf("$TALKING *Facedesks~*")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("drift"), "Drift Command", "*Initial D intensifies.*"),
            GetImage(type = "initial_d", fileType = GIF),
            listOf("$CAR *Drifts in japanese~*")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("thinking"), "Thinking Command", "Sends a random thinking image."),
            GetImage(type = "thinking")
        )


        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("nom"), "Nom Command", "Sends a random nom image."),
            GetImage(type = "nom")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("thumbsup", "thumps"), "Thumbs Up Command", "Sends a random Thumbs Up image."),
            GetImage(type = "thumbsup"),
            listOf(
                "$THUMBSUP1 *Raises hand~*",
                "$THUMBSUP2 *Raises hand~*",
                "$THUMBSUP3 *Raises hand~*",
                "$THUMBSUP4 *Raises hand~*",
                "$THUMBSUP5 *Raises hand~*",
                "$THUMBSUP6 *Raises hand~*"
            )
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("lewd"), "Lewd Command", "T-that's lewd!"),
            GetImage(type = "lewd"),
            ActionLines(
                "$LEWD {mentions}, Y-you lewdie!",
                "$LEWD Y-you lewdie!",
                "$LEWD Y-you lewdie!",
                "$LEWD I-I'm not lewd, you lewdie!"
            )
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("owo"), "OwO Command", "What's this?"),
            GetImage(type = "owo")
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("dab"), "Dabs Command", "Sends a random dab image."),
            GetImage(type = "dab")
        )

        WeebCommand.Action(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("shrug"), "Shrug Command", "Sends a random shrug image."),
            GetImage(type = "shrug", fileType = GIF),
            ActionLines(
                "$SHRUG {mentions}, {author} is shrugging at you",
                "$SHRUG {author} is shrugging",
                "$SHRUG {author} is shrugging",
                "$SHRUG W-Why are you Shrugging at me?"
            )
        )

        WeebCommand.Image(
            category, weebProvider, r, cache,
            WeebCommandInfo(listOf("awoo", "awo", "awooo", "awoooo"), "Awoo Command", "Sends a random awoo!"),
            GetImage(type = "awoo", fileType = GIF),
            listOf(
                "$AWOO Awooo~!"
            )
        )

        CustomActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("meow"), "Meow Command", "Meows at the mentioned users.", "meow.gif"),
            assetProvider["assets/aru/actions/meow.txt"],
            ActionLines(
                "$CAT {mentions}, Meow",
                "$CAT *Meow~*",
                "$CAT {author}, *Meow~*",
                "$CAT Eeh..? T-that's awkward, stop!"
            )
        )

        CustomActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("beg"), "Beg Command", "Begs the mentioned users.", "beg.gif"),
            assetProvider["assets/aru/actions/beg.txt"],
            ActionLines(
                "$FUCK {author} is begging {mentions}",
                "$FUCK *Begs~*",
                "$FUCK *Begs you~*",
                "$FUCK Eeh..? Why are you begging me? uwu"
            )
        )

        CustomActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("bloodsuck", "vampire"), "Bloodsuck Command", "Sucks the blood of the mentioned users.", "bloodsuck.gif"),
            assetProvider["assets/aru/actions/bloodsuck.txt"],
            ActionLines(
                "$BITE {mentions}, {author} is sucking your blood",
                "$BITE *Sucks blood~*",
                "$BITE *Sucks your blood~*",
                "$BITE Eeh..? *gets blood sucked~* T-that hurts, stop!"
            )
        )

        CustomActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("fuck"), "Fuck Command", "Fucks the mentioned users... Hey, that's lewd!", "fuck.gif", nsfw = true),
            assetProvider["assets/aru/actions/fuck.txt"],
            ActionLines(
                "$FUCK {author} is fucking {mentions}",
                "$FUCK *Fucks~*",
                "$FUCK *Fucks you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )
    }

}