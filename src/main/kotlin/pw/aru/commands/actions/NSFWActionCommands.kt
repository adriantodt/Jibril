package pw.aru.commands.actions

import com.github.natanbc.weeb4j.Weeb4J
import pw.aru.commands.actions.impl.ActionCommandsWorkshop
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.help.prefix
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.URLCache
import pw.aru.utils.text.*

@CommandProvider
class NSFWActionCommands(
    weebApi: Weeb4J,
    cache: URLCache,
    private val assetProvider: ReloadableListProvider
) : ActionCommandsWorkshop(weebApi, cache, Category.NSFW_ACTION) {
    override fun create() {
        //69
        actionCommand(listOf("69"), "69 Command", "Hey, that's lewd!") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/69.txt"])
            actions(
                "$FUCK {author} and {mentions} are uh... doing lewd stuff.",
                "",
                "$FUCK Lewdie~",
                "$FUCK Eeh..? What's this sausage...? H-hey, stop touching there, that's lewd! Bakaaa!"
            )
        }

        //anal
        actionCommand(
            listOf("anal", "fuckanal"),
            "Anal Command",
            "Fucks the ass of the mentioned users... Hey, that's lewd!"
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/anal.txt"])
            actions(
                "$FUCK {author} is fucking {mentions}'s ass",
                "",
                "$FUCK *Fucks your ass~* Lewdie~",
                "$FUCK H-HEY! M-my ass! T-that hurts, bakaaa! Stahp!"
            )
        }

        //analcum
        actionCommand(
            listOf("analcum", "cumanal", "fuckanalcum"),
            "Anal Cum Command",
            "Cums inside... Okay, that's enough. And lewd."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/analcum.txt"])
            actions(
                "$FUCK {author} is cumming inside {mentions}'s ass",
                "",
                "$FUCK *Cums in your ass~* Lewdie~",
                "$FUCK EEH? D-DON'T... $CUM Baka, I said I don't want to be filled up!"
            )
        }

        //analplay
        actionCommand(
            listOf("analplay", "analforeplay", "foreplayanal"),
            "Anal Play Command",
            "Plays with... Okay, that's enough. And lewd."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/analplay.txt"])
            actions(
                "$FUCK {author} is playing with {mentions}'s ass",
                "",
                "$FUCK *Plays with your ass~* Lewdie~",
                "$FUCK H-HEY! M-my ass! That's lewd! Bakaaa!"
            )
        }

        //blowjob
        actionCommand(listOf("blowjob"), "Blowjob Command", "Sucks the dick of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/blowjob.txt"])
            actions(
                "$FUCK {author} is sucking {mentions}'s dick",
                "",
                "$FUCK W-What? Am I supposed to...? *starts sucking you~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        }

        //blowjobcum
        actionCommand(
            listOf("blowjobcum", "cumblowjob"),
            "Blowjob Cum Command",
            "Cums while someone is doing you a blowjob."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/blowjobcum.txt"])
            actions(
                "$FUCK {author} is sucking {mentions}'s dick... $CUM",
                "",
                "$FUCK *Swallows~* Could you like... not cum while I'm sucking you, baka?",
                "$GASM S-something is c-coming out... $CUM"
            )
        }

        //boobjob
        actionCommand(listOf("boobjob", "titjob"), "Boobjob Command", "Gives a boobjob to the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/boobjob.txt"])
            actions(
                "$BOOBS {author} is giving {mentions} a boobjob",
                "",
                "$BOOBS W-What? Am I supposed to...? *gives you a boobjob~* Lewdie~",
                "$BOOBS Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        }

        //boobjobcum
        actionCommand(
            listOf("boobjobcum", "cumboobjob", "titjobcum", "cumtitjob"),
            "Boobjob Cum Command",
            "Cums while someone is doing you a boobjob."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/boobjobcum.txt"])
            actions(
                "$BOOBS {author} is giving {mentions} a boobjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$BOOBS Could you like... not cum on my face while I'm playing with you, baka?"
            )
        }

        //booblick
        actionCommand(listOf("booblick"), "Booblick Command", "Licks the boobs of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/booblick.txt"])
            actions(
                "$BOOBS {author} is licking {mentions}'s boobs",
                "",
                "$BOOBS *Licks your boobies~* Lewdiee~",
                "$GASM M-my nipples are sensitive, c-can you not, baka?"
            )
        }

        //boobplay
        actionCommand(listOf("boobplay"), "Boobplay Command", "Plays with the boobs of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/boobplay.txt"])
            actions(
                "$BOOBS {author} is playing with {mentions}'s boobs",
                "",
                "$BOOBS *Plays with your boobies~* Lewdiee~",
                "$BOOBS Eeh..? W-Why are you playing with my boobs? That's lewd, baka!"
            )
        }

        //boobtease
        actionCommand(listOf("boobtease"), "Boobtease Command", "Teases the mentioned users using your boobs.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/boobtease.txt"])
            actions(
                "$BOOBS {author} is using her melons to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$BOOBS Eeh..? W-Why are you teasing me?"
            )
        }

        //buttplay
        actionCommand(listOf("buttplay"), "Buttplay Command", "Plays with the butts of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/buttplay.txt"])
            actions(
                "$BUTTS {author} is playing with {mentions}'s butts",
                "",
                "$BUTTS *Plays with your butts~* Lewdiee~",
                "$BUTTS Eeh..? H-HEY! M-my butt! That's lewd! Bakaaa!"
            )
        }

        //butttease
        actionCommand(listOf("butttease"), "Butttease Command", "Teases the mentioned users using your butts.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/butttease.txt"])
            actions(
                "$BUTTS {author} is using her butts to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$BUTTS Eeh..? W-Why are you teasing me?"
            )
        }

        //cocktease
        actionCommand(listOf("cocktease"), "Cocktease Command", "Teases the cock of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/cocktease.txt"])
            actions(
                "$FUCK {mentions}, {author} is teasing your cock",
                "",
                "$FUCK W-What? Am I supposed to...? *teases your cock~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        }

        //cum
        actionCommand(
            listOf("cum", "fuckcum", "cumfuck"),
            "Cum (Classical) Command",
            "Cums inside the mentioned users' pussy. That's messy... And lewd.\nCheck `${prefix}gasm` too!"
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/cum.txt"])
            actions(
                "$FUCK {mentions}, {author} is cumming inside you",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK EEH? D-DON'T... $CUM Baka, I said I don't want to be filled up!"
            )
        }

        //feetjob
        actionCommand(listOf("feetjob", "footjob"), "Feetjob Command", "Gives a feetjob to the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/feetjob.txt"])
            actions(
                "$FUCK {author} is giving {mentions} a feetjob",
                "",
                "$FUCK W-What? Am I supposed to...? *gives you a feetjob~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        }

        //feetjobcum
        actionCommand(
            listOf("feetjobcum", "cumfeetjob", "footjobcum", "cumfootjob"),
            "Feetjob Cum Command",
            "Cums while someone is doing you a feetjob."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/feetjobcum.txt"])
            actions(
                "$FUCK {author} is giving {mentions} a feetjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK Could you like... not cum on my feet? Now lick it, you baka!"
            )
        }

        //fuck
        actionCommand(listOf("fuck"), "Fuck (Classical) Command", "Fucks the mentioned users... Hey, that's lewd!") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/fuck.txt"])
            actions(
                "$FUCK {author} is fucking {mentions}",
                "$FUCK *Fucks~*",
                "$FUCK *Fucks you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        }

        //fucktease
        actionCommand(
            listOf("fucktease"),
            "Fucktease Command",
            "Teases the mentioned users... God, you're really lewd."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/fucktease.txt"])
            actions(
                "$FUCK {mentions}, {author} is teasing you to fuck him",
                "",
                "$FUCK Eeh..? What? Since when I have a dick?? W-What? Am I supposed to...? *teases you~* I don't even know anymore.",
                "$FUCK Eeh..? W-Why are you teasing me? Stop, that's lewd, Bakaaa!"
            )
        }

        //gangbang
        actionCommand(listOf("gangbang"), "Gangbang Command", "Gangbangs the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/gangbang.txt"])
            actions(
                "$FUCK {everyone} are having a gangbang",
                "",
                "$FUCK I brought some friends. And apparently I have a dick now, too. Oh, well.. *starts gangbanging you~*",
                "$FUCK Eeh..? Who's all these... guys? *gets gangbanged~* $CRY That's not funny..."
            )
        }

        //gasm
        actionCommand(
            listOf("gasm", "orgasm"),
            "Orgasm Command",
            "When you're a girl and someone breaks your decency. That's messy... And lewd.\nCheck `${prefix}cum` too!"
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/gasm.txt"])
            actions(
                "$GASM {mentions}, {author} is having an orgasm",
                "",
                "$GASM {author} is having an orgasm",
                "$GASM Having fun? *pats~* $PAT"
            )
        }

        //handjob
        actionCommand(listOf("handjob"), "Handjob Command", "Gives a handjob to the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/handjob.txt"])
            actions(
                "$FUCK {author} is giving {mentions} a handjob",
                "",
                "$FUCK W-What? Am I supposed to...? *gives you a handjob~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd! B-but don't stop!"
            )
        }

        //handjobcum
        actionCommand(
            listOf("handjobcum", "cumhandjob"),
            "Handjob Cum Command",
            "Cums while someone is doing you a handjob."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/handjobcum.txt"])
            actions(
                "$FUCK {author} is giving {mentions} a handjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK Could you like... not cum on my hand? Now lick it, you baka!"
            )
        }

        //lesbian
        actionCommand(listOf("lesbian"), "Lesbian Command", "Lesbian fun, together.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/lesbian.txt"])
            actions(
                "$FUCK {everyone} are having some decent fun together",
                "",
                "$FUCK *plays with you~* Lewdie~",
                "$FUCK *plays with you~* Lewdie~"
            )
        }

        //masturbate
        actionCommand(listOf("masturbate"), "Masturbate Command", "When you're a girl and you're just way too bored.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/masturbating.txt"])
            actions(
                "$GASM {mentions}, {author} is masturbating in front of you",
                "",
                "$GASM {author} is masturbating",
                "$GASM Having fun? *pats~* $PAT"
            )
        }

        //pussylick
        actionCommand(listOf("pussylick"), "Pussylick Command", "Licks the pussy of the mentioned users") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/pussylick.txt"])
            actions(
                "$FUCK {author} is licking {mentions}'s pussy",
                "",
                "$FUCK *Licks your pussy~* Lewdiee~",
                "$GASM You're good with the tongue, heh?"
            )
        }

        //pussyplay
        actionCommand(listOf("pussyplay"), "Pussyplay Command", "Plays with the boobs of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/pussyplay.txt"])
            actions(
                "$FUCK {author} is playing with {mentions}'s pussy",
                "",
                "$FUCK *Plays with your pussy~* Lewdiee~",
                "$FUCK Eeh..? W-Why are you playing with my pussy? That's lewd, baka!"
            )
        }

        //pussytease
        actionCommand(listOf("pussytease"), "Pussytease Command", "Teases the mentioned users using your pussy.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/pussytease.txt"])
            actions(
                "$FUCK {author} is using her pussy to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$FUCK Eeh..? W-Why are you teasing me?"
            )
        }

        //spank
        actionCommand(listOf("spank"), "Spank Command", "Spanks the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/spank.txt"])
            actions(
                "$FUCK {author} is spanking {mentions}",
                "",
                "$FUCK *Spanks you~* BAKAA!",
                "$CRY Don't spank mee~ I did nothing wroong. $CRY"
            )
        }

        //stripclothes
        actionCommand(listOf("stripclothes"), "Strip Clothes Command", "Strips the clothes of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/stripclothes.txt"])
            actions(
                "$FUCK {author} is stripping {mentions}'s clothes",
                "",
                "$FUCK *strips your clothes~* Lewdiee~",
                "$FUCK D-DON'T... *gets clothes stripped~* Y-you bakaa!"
            )
        }

        //striptease
        actionCommand(listOf("striptease"), "Striptease Command", "Stripteases in front of the mentioned users.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/striptease.txt"])
            actions(
                "$FUCK {mentions}, {author} is strip-teasing in front of you",
                "",
                "$FUCK {author} is stripteasing you",
                "$FUCK *stripteases in front of you~* I-I'm not doing this because you asked, b-baka."
            )
        }

        //tentacles
        actionCommand(listOf("tentacles"), "Tentacles Command", "Tentacle fun.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/tentacles.txt"])
            actions(
                "$FUCK {mentions}, you're getting fucked by {author}'s tentacles",
                "",
                "$FUCK {author} is getting fucked by tentacles",
                "$FUCK W-Wha..? *gets fucked by tentacles~* $CRY That's not funny..."
            )
        }

        //toying
        actionCommand(listOf("toying"), "Toying Command", "When you're a girl and you're REALLY bored.") {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/toying.txt"])
            actions(
                "$GASM {mentions}, {author} is toying herself in front of you",
                "",
                "$GASM {author} is toying herself",
                "$GASM Having fun? *pats~* $PAT"
            )
        }

        //weirdfuck
        actionCommand(
            listOf("weirdfuck", "sillyfuck"),
            "Weird Fuck Command",
            "Fucks the mentioned users... in a very weird way."
        ) {
            nsfwProvider = fromLinks(assetProvider["assets/aru/nsfw_actions/weirdfuck.txt"])
            actions(
                "$FUCK {mentions}, {author} is fucking you in a... rather weird way",
                "",
                "$FUCK Eeh..? Since when I have a dick?? Am I supposed to...? *fucks you in a weird way~* I don't even know what am I doing.",
                "$FUCK Wha?? *gets confused while getting fucked~* What the fuck are you doing with me...?"
            )
        }
    }
}