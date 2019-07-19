package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType.GIF
import pw.aru.commands.actions.impl.ActionCommandsWorkshop
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache
import pw.aru.utils.text.*

@CommandProvider
class ActionCommands(
    weebApi: Weeb4J,
    cache: URLCache,
    private val assetProvider: ReloadableListProvider
) : ActionCommandsWorkshop(weebApi, cache, Category.ACTION) {

    /*A
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

    override fun create() {

        actionCommand(listOf("pat"), "Pat Command", "Pats the mentioned users.") {
            provider = fromWeebSh(type = "pat", fileType = GIF)
            actions(
                anyTarget = "$PAT {mentions}, you have been patted by {author}",
                noTargets = "$PAT *Pats~*",
                targetsYou = "$PAT *Pats you~*",
                targetsMe = "$PAT Oh, eh.. *~gets patted~* T-thanks~"
            )
        }

        actionCommand(listOf("poke"), "Poke Command", "Pokes the mentioned users.") {
            provider = fromWeebSh(type = "poke", fileType = GIF)
            actions(
                anyTarget = "$POKE {mentions}, {author} is poking you",
                noTargets = "$POKE *Pokes~*",
                targetsYou = "$POKE *Pokes you~*",
                targetsMe = "$POKE Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("highfive"), "Highfive Command", "Highfives the mentioned users.") {
            provider = fromWeebSh(type = "highfive", fileType = GIF)
            actions(
                anyTarget = "$HIGHFIVE {author} is high-fiving {mentions}",
                noTargets = "$HIGHFIVE *Highfives~*",
                targetsYou = "$HIGHFIVE *Highfives you~*",
                targetsMe = "$HIGHFIVE Oh, eh.. *~high-fives back~* Hai!"
            )
        }

        actionCommand(listOf("tease", "teehee"), "Tease Command", "Teases the mentioned users.") {
            provider = fromWeebSh(type = "teehee", fileType = GIF)
            actions(
                anyTarget = "$TEEHEE {mentions}, {author} is teasing you",
                noTargets = "$TEEHEE *Teases~*",
                targetsYou = "$TEEHEE *Teases you~*",
                targetsMe = "$TEEHEE Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("tickle"), "Tickle Command", "Tickles the mentioned users.") {
            provider = fromWeebSh(type = "teehee", fileType = GIF)
            actions(
                anyTarget = "$TEEHEE {mentions}, {author} is tickling you",
                noTargets = "$TEEHEE *Tickles~*",
                targetsYou = "$TEEHEE *Tickles you~*",
                targetsMe = "$TEEHEE Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("slap"), "Slap Command", "Slaps the mentioned users.") {
            provider = fromWeebSh(type = "slap", fileType = GIF)
            actions(
                anyTarget = "$EVIL {mentions}, you have been slapped by {author}",
                noTargets = "$EVIL *Slaps~*",
                targetsYou = "$EVIL *Slaps you~*",
                targetsMe = "$EVIL Eeh..? *~gets slapped~* Y-you BAKA! $CRY"
            )
        }

        actionCommand(listOf("smile"), "Smile Command", "Smiles at the mentioned users.") {
            provider = fromWeebSh(type = "smile", fileType = GIF)
            actions(
                anyTarget = "$SMILE {mentions}, {author} is smiling at you",
                noTargets = "$SMILE *Smiles~*",
                targetsYou = "$SMILE *Smiles at you~*",
                targetsMe = "$SMILE Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("pout"), "Pout Command", "Pouts at the mentioned users.") {
            provider = fromWeebSh(type = "pout", fileType = GIF)
            actions(
                anyTarget = "$POUT {mentions}, {author} is pouting at you",
                noTargets = "$POUT *Pouts~*",
                targetsYou = "$POUT *Pouts at you~*",
                targetsMe = "$POUT Eeh..? W-what do you want?"
            )
        }

        actionCommand(listOf("cuddle"), "Cuddle Command", "Cuddles the mentioned users.") {
            provider = fromWeebSh(type = "cuddle", fileType = GIF)
            actions(
                anyTarget = "$CUDDLE {mentions}, you have been cuddled by {author}",
                noTargets = "$CUDDLE *Cuddles~*",
                targetsYou = "$CUDDLE *Cuddles you~*",
                targetsMe = "$CUDDLE Oh, eh.. *~gets cuddled~* T-thanks~"
            )
        }

        actionCommand(listOf("punch"), "Punch Command", "Punches the mentioned users.") {
            provider = fromWeebSh(type = "punch", fileType = GIF)
            actions(
                anyTarget = "$PUNCH {mentions}, you have been punched by {author}",
                noTargets = "$PUNCH *Punches~*",
                targetsYou = "$PUNCH *Punches you~*",
                targetsMe = "$PUNCH Eeh..? *~gets punched~* Y-you BAKA! $CRY"
            )
        }

        actionCommand(listOf("holdhands", "holdhand"), "Hold Hands Command", "Holds the hands of the mentioned users.") {
            provider = fromWeebSh(type = "handholding", fileType = GIF)
            actions(
                anyTarget = "$CUDDLE {author} is holding {mentions}'s hand",
                noTargets = "$CUDDLE *Holds hands~*",
                targetsYou = "$CUDDLE *Holds your hand~*",
                targetsMe = "$CUDDLE Oh, eh.. T-thanks~ $BLUSH"
            )
        }

        actionCommand(listOf("hug"), "Hug Command", "Hugs the mentioned users.") {
            provider = fromWeebSh(type = "hug", fileType = GIF)
            actions(
                anyTarget = "$HUG {mentions}, you have been hugged by {author}",
                noTargets = "$HUG *Hugs~*",
                targetsYou = "$HUG *Hugs you~*",
                targetsMe = "$HUG Oh, eh.. *~gets hugged~* T-thanks~"
            )
        }

        actionCommand(listOf("nuzzle"), "Nuzzle Command", "Nuzzles the mentioned users.") {
            provider = fromLinks(assetProvider["assets/aru/actions/nuzzle.txt"])
            actions(
                anyTarget = "$CUDDLE {mentions}, you have been nuzzled by {author}",
                noTargets = "$CUDDLE *Nuzzles~*",
                targetsYou = "$CUDDLE *Nuzzles you~*",
                targetsMe = "$CUDDLE Oh, eh.. *~gets nuzzled~* T-thanks~"
            )
        }

        actionCommand(listOf("kiss"), "Kiss Command", "Kisses the mentioned users.") {
            provider = fromWeebSh(type = "kiss", fileType = GIF)
            actions(
                anyTarget = "$KISS {mentions}, you have been kissed by {author}",
                noTargets = "$KISS *Kisses~*",
                targetsYou = "$KISS *Kisses you~*",
                targetsMe = "$KISS Oh, eh.. *~gets kissed~* T-thanks~"
            )
        }

        actionCommand(listOf("lick"), "Lick Command", "Licks the mentioned users.") {
            provider = fromWeebSh(type = "lick", fileType = GIF)
            actions(
                anyTarget = "$LICK {mentions}, {author} is licking you",
                noTargets = "$LICK *Licks~*",
                targetsYou = "$LICK *Licks you~*",
                targetsMe = "$LICK Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("bite"), "Bite Command", "Bite the mentioned users.") {
            provider = fromWeebSh(type = "bite", fileType = GIF)
            actions(
                anyTarget = "$BITE {mentions}, you have been bitten by {author}",
                noTargets = "$BITE *Bites~*",
                targetsYou = "$BITE *Bites you~*",
                targetsMe = "$BITE O-Ouch! That hurts, you baka!"
            )
        }

        actionCommand(listOf("stare"), "Stare Command", "Stares the mentioned users.") {
            provider = fromWeebSh(type = "stare", fileType = GIF)
            actions(
                anyTarget = "$STARE {mentions}, {author} is staring you",
                noTargets = "$STARE *Stares~*",
                targetsYou = "$STARE *Stares you~*",
                targetsMe = "$STARE Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("cry"), "Cry Command", "It's okay to cry.") {
            provider = fromWeebSh(type = "cry", fileType = GIF)
            actions(
                anyTarget = "$CRY {mentions}, {author} is crying",
                noTargets = "$CRY *Cries~*",
                targetsYou = "$CRY *Cries~*",
                targetsMe = "$CRY Eeh..? D-Did I do something wrong? I-I'm sorry!"
            )
        }

        actionCommand(listOf("smug"), "Smug Command", "It's never late enough to be smug.") {
            provider = fromWeebSh(type = "smug", fileType = GIF)
            actions(
                anyTarget = "$SMUG {mentions}, {author} is looking a bit smug",
                noTargets = "$SMUG {author} is looking a bit smug",
                targetsYou = "$SMUG {author} is looking a bit smug",
                targetsMe = "$SMUG Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("wag", "tail", "tailwag", "wagtail"), "Wag Tail Command", "Wags tails at the mentioned users.") {
            provider = fromWeebSh(type = "wag", fileType = GIF)
            actions(
                anyTarget = "$AWOO {mentions}, {author} is wagging the tail at you!",
                noTargets = "$AWOO *Wags tail~*",
                targetsYou = "$AWOO *Wags tail at you~*",
                targetsMe = "$AWOO Eeh..? W-what do you want?"
            )
        }

        imageCommand(listOf("blush"), "Blush Command", "When it's just too much for you to handle.") {
            provider = fromWeebSh(type = "banghead", fileType = GIF)
            messages("$BLUSH {author} is slowly turning into a tomato")
        }

        imageCommand(listOf("dance"), "Dance Command", "Sends a random dance image.") {
            provider = fromWeebSh(type = "dance", fileType = GIF)
            messages("$DANCE {author} is dancing $DANCE2")
        }

        imageCommand(listOf("facedesk", "banghead"), "Facedesk Command", "When it's just too much to handle.") {
            provider = fromWeebSh(type = "banghead", fileType = GIF)
            messages("$TALKING *Facedesks~*")
        }

        imageCommand(listOf("drift"), "Drift Command", "*Initial D intensifies.*") {
            provider = fromWeebSh(type = "initial_d", fileType = GIF)
            messages("$CAR *Drifts in japanese~*")
        }

        imageCommand(listOf("thinking"), "Thinking Command", "Sends a random thinking image.") {
            provider = fromWeebSh(type = "thinking")
        }

        imageCommand(listOf("nom"), "Nom Command", "Sends a random nom image.") {
            provider = fromWeebSh(type = "nom")
        }

        imageCommand(listOf("thumbsup", "thumps"), "Thumbs Up Command", "Sends a random Thumbs Up image.") {
            provider = fromWeebSh(type = "thumbsup")
            messages(
                "$THUMBSUP1 *Raises hand~*",
                "$THUMBSUP2 *Raises hand~*",
                "$THUMBSUP3 *Raises hand~*",
                "$THUMBSUP4 *Raises hand~*",
                "$THUMBSUP5 *Raises hand~*",
                "$THUMBSUP6 *Raises hand~*"
            )
        }

        actionCommand(listOf("lewd"), "Lewd Command", "T-that's lewd!") {
            provider = fromWeebSh(type = "lewd")
            actions(
                anyTarget = "$LEWD {mentions}, Y-you lewdie!",
                noTargets = "$LEWD Y-you lewdie!",
                targetsYou = "$LEWD Y-you lewdie!",
                targetsMe = "$LEWD I-I'm not lewd, you lewdie!"
            )
        }

        imageCommand(listOf("owo"), "OwO Command", "What's this?") {
            provider = fromWeebSh(type = "owo")
        }

        imageCommand(listOf("dab"), "Dabs Command", "Sends a random dab image.") {
            provider = fromWeebSh(type = "dab")
        }

        actionCommand(listOf("shrug"), "Shrug Command", "Sends a random shrug image.") {
            provider = fromWeebSh(type = "shrug", fileType = GIF)
            actions(
                anyTarget = "$SHRUG {mentions}, {author} is shrugging at you",
                noTargets = "$SHRUG {author} is shrugging",
                targetsYou = "$SHRUG {author} is shrugging",
                targetsMe = "$SHRUG W-Why are you Shrugging at me?"
            )
        }

        imageCommand(listOf("awoo", "awo", "awooo", "awoooo", "awooooo", "awoooooo", "awoooooo"), "Awoo Command", "Sends a random awoo!") {
            provider = fromWeebSh(type = "awoo", fileType = GIF)
            messages("$AWOO Awooo~!")
        }

        actionCommand(listOf("meow"), "Meow Command", "Meows at the mentioned users.") {
            provider = fromLinks(assetProvider["assets/aru/actions/meow.txt"])
            actions(
                anyTarget = "$CAT {mentions}, Meow",
                noTargets = "$CAT *Meow~*",
                targetsYou = "$CAT {author}, *Meow~*",
                targetsMe = "$CAT Eeh..? T-that's awkward, stop!"
            )
        }

        actionCommand(listOf("beg"), "Beg Command", "Begs the mentioned users.") {
            provider = fromLinks(assetProvider["assets/aru/actions/beg.txt"])
            actions(
                anyTarget = "$BEG {author} is begging {mentions}",
                noTargets = "$BEG *Begs~*",
                targetsYou = "$BEG *Begs you~*",
                targetsMe = "$BEG Eeh..? Why are you begging me? uwu"
            )
        }

        actionCommand(listOf("bloodsuck", "vampire"), "Bloodsuck Command", "Sucks the blood of the mentioned users.") {
            provider = fromLinks(assetProvider["assets/aru/actions/bloodsuck.txt"])
            actions(
                anyTarget = "$BITE {mentions}, {author} is sucking your blood",
                noTargets = "$BITE *Sucks blood~*",
                targetsYou = "$BITE *Sucks your blood~*",
                targetsMe = "$BITE Eeh..? *gets blood sucked~* T-that hurts, stop!"
            )
        }

        //eartease
        actionCommand(listOf("eartease", "teaseear", "teaseears"), "Ear Tease Command", "Ear-teases the mentioned users.") {
            provider = fromLinks(assetProvider["assets/aru/sfw_actions/eartease.txt"])
            actions(
                anyTarget = "$TEEHEE {author} is teasing {mentions}'s ear",
                noTargets = "$TEEHEE *Teases ear~*",
                targetsYou = "$TEEHEE *Teases your ear~*",
                targetsMe = "$TEEHEE Eeh..? *starts melting~* I-it's not I'm liking it, b-baka!"
            )
        }

        //necktease
        actionCommand(listOf("necktease", "teaseneck"), "Neck Tease Command", "Neck-teases the mentioned-users") {
            provider = fromLinks(assetProvider["assets/aru/sfw_actions/necktease.txt"])
            actions(
                anyTarget = "$TEEHEE {author} is teasing {mentions}'s neck",
                noTargets = "$TEEHEE *Teases neck~*",
                targetsYou = "$TEEHEE *Teases your neck~*",
                targetsMe = "$TEEHEE Eeh..? Eeh..? *starts melting~* I-it's not I'm liking it, b-baka!"
            )
        }
    }
}