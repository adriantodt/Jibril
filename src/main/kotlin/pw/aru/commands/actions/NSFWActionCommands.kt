package pw.aru.commands.actions

import okhttp3.OkHttpClient
import pw.aru.commands.actions.base.ActionLines
import pw.aru.commands.actions.base.CustomCommandInfo
import pw.aru.commands.actions.base.URLsActionCommand
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.core.commands.help.prefix
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.*
import java.io.File

@CommandProvider
class NSFWActionCommands(
    httpClient: OkHttpClient,
    private val assetProvider: ReloadableListProvider
) : ICommandProvider {

    private val cache = URLCache(httpClient, File("url_cache"))

    override fun provide(r: CommandRegistry) {
        val category = Category.NSFW_ACTION

        //69
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("69"), "69 Command", "Hey, that's lewd!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/69.txt"],
            ActionLines(
                "$FUCK {author} and {mentions} are uh... doing lewd stuff.",
                "",
                "$FUCK Lewdie~",
                "$FUCK Eeh..? What's this sausage...? H-hey, stop touching there, that's lewd! Bakaaa!"
            )
        )

        //anal
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("anal", "fuckanal"), "Anal Command", "Fucks the ass of the mentioned users... Hey, that's lewd!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/anal.txt"],
            ActionLines(
                "$FUCK {author} is fucking {mentions}'s ass",
                "",
                "$FUCK *Fucks your ass~* Lewdie~",
                "$FUCK H-HEY! M-my ass! T-that hurts, bakaaa! Stahp!"
            )
        )

        //analcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("analcum", "cumanal", "fuckanalcum"), "Anal Cum Command", "Cums inside... Okay, that's enough. And lewd.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/analcum.txt"],
            ActionLines(
                "$FUCK {author} is cumming inside {mentions}'s ass",
                "",
                "$FUCK *Cums in your ass~* Lewdie~",
                "$FUCK EEH? D-DON'T... $CUM Baka, I said I don't want to be filled up!"
            )
        )

        //analplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("analplay", "analforeplay", "foreplayanal"), "Anal Play Command", "Plays with... Okay, that's enough. And lewd.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/analplay.txt"],
            ActionLines(
                "$FUCK {author} is playing with {mentions}'s ass",
                "",
                "$FUCK *Plays with your ass~* Lewdie~",
                "$FUCK H-HEY! M-my ass! That's lewd! Bakaaa!"
            )
        )

        //blowjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("blowjob"), "Blowjob Command", "Sucks the dick of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/blowjob.txt"],
            ActionLines(
                "$FUCK {author} is sucking {mentions}'s dick",
                "",
                "$FUCK W-What? Am I supposed to...? *starts sucking you~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        )

        //blowjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("blowjobcum", "cumblowjob"), "Blowjob Cum Command", "Cums while someone is doing you a blowjob.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/blowjobcum.txt"],
            ActionLines(
                "$FUCK {author} is sucking {mentions}'s dick... $CUM",
                "",
                "$FUCK *Swallows~* Could you like... not cum while I'm sucking you, baka?",
                "$GASM S-something is c-coming out... $CUM"
            )
        )

        //boobjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobjob", "titjob"), "Boobjob Command", "Gives a boobjob to the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobjob.txt"],
            ActionLines(
                "$BOOBS {author} is giving {mentions} a boobjob",
                "",
                "$BOOBS W-What? Am I supposed to...? *gives you a boobjob~* Lewdie~",
                "$BOOBS Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        )

        //boobjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobjobcum", "cumboobjob", "titjobcum", "cumtitjob"), "Boobjob Cum Command", "Cums while someone is doing you a boobjob.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobjobcum.txt"],
            ActionLines(
                "$BOOBS {author} is giving {mentions} a boobjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$BOOBS Could you like... not cum on my face while I'm playing with you, baka?"
            )
        )

        //booblick
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("booblick"), "Booblick Command", "Licks the boobs of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/booblick.txt"],
            ActionLines(
                "$BOOBS {author} is licking {mentions}'s boobs",
                "",
                "$BOOBS *Licks your boobies~* Lewdiee~",
                "$GASM M-my nipples are sensitive, c-can you not, baka?"
            )
        )

        //boobplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobplay"), "Boobplay Command", "Plays with the boobs of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobplay.txt"],
            ActionLines(
                "$BOOBS {author} is playing with {mentions}'s boobs",
                "",
                "$BOOBS *Plays with your boobies~* Lewdiee~",
                "$BOOBS Eeh..? W-Why are you playing with my boobs? That's lewd, baka!"
            )
        )

        //boobtease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobtease"), "Boobtease Command", "Teases the mentioned users using your boobs.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobtease.txt"],
            ActionLines(
                "$BOOBS {author} is using her melons to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$BOOBS Eeh..? W-Why are you teasing me?"
            )
        )

        //buttplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("buttplay"), "Buttplay Command", "Plays with the butts of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/buttplay.txt"],
            ActionLines(
                "$BUTTS {author} is playing with {mentions}'s butts",
                "",
                "$BUTTS *Plays with your butts~* Lewdiee~",
                "$BUTTS Eeh..? H-HEY! M-my butt! That's lewd! Bakaaa!"
            )
        )

        //butttease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("butttease"), "Butttease Command", "Teases the mentioned users using your butts.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/butttease.txt"],
            ActionLines(
                "$BUTTS {author} is using her butts to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$BUTTS Eeh..? W-Why are you teasing me?"
            )
        )

        //cocktease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("cocktease"), "Cocktease Command", "Teases the cock of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/cocktease.txt"],
            ActionLines(
                "$FUCK {mentions}, {author} is teasing your cock",
                "",
                "$FUCK W-What? Am I supposed to...? *teases your cock~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        )

        //cum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("cum", "fuckcum", "cumfuck"), "Cum (Classical) Command", "Cums inside the mentioned users' pussy. That's messy... And lewd.\nCheck `${prefix}gasm` too!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/cum.txt"],
            ActionLines(
                "$FUCK {mentions}, {author} is cumming inside you",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK EEH? D-DON'T... $CUM Baka, I said I don't want to be filled up!"
            )
        )

        //feetjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("feetjob", "footjob"), "Feetjob Command", "Gives a feetjob to the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/feetjob.txt"],
            ActionLines(
                "$FUCK {author} is giving {mentions} a feetjob",
                "",
                "$FUCK W-What? Am I supposed to...? *gives you a feetjob~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd!"
            )
        )

        //feetjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("feetjobcum", "cumfeetjob", "footjobcum", "cumfootjob"), "Feetjob Cum Command", "Cums while someone is doing you a feetjob.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/feetjobcum.txt"],
            ActionLines(
                "$FUCK {author} is giving {mentions} a feetjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK Could you like... not cum on my feet? Now lick it, you baka!"
            )
        )

        //fuck
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("fuck"), "Fuck (Classical) Command", "Fucks the mentioned users... Hey, that's lewd!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/fuck.txt"],
            ActionLines(
                "$FUCK {author} is fucking {mentions}",
                "$FUCK *Fucks~*",
                "$FUCK *Fucks you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //fucktease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("fucktease"), "Fucktease Command", "Teases the mentioned users... God, you're really lewd.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/fucktease.txt"],
            ActionLines(
                "$FUCK {mentions}, {author} is teasing you to fuck him",
                "",
                "$FUCK Eeh..? What? Since when I have a dick?? W-What? Am I supposed to...? *teases you~* I don't even know anymore.",
                "$FUCK Eeh..? W-Why are you teasing me? Stop, that's lewd, Bakaaa!"
            )
        )

        //gangbang
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("gangbang"), "Gangbang Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/gangbang.txt"],
            ActionLines(
                "$FUCK {everyone} are having a gangbang",
                "",
                "$FUCK I brought some friends. And apparently I have a dick now, too. Oh, well.. *starts gangbanging you~*",
                "$FUCK Eeh..? Who's all these... guys? *gets gangbanged~* $CRY That's not funny..."
            )
        )

        //gasm
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("gasm", "orgasm"), "Orgasm Command", "When you're a girl and someone breaks your decency. That's messy... And lewd.\nCheck `${prefix}cum` too!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/gasm.txt"],
            ActionLines(
                "$GASM {mentions}, {author} is having an orgasm",
                "",
                "$GASM {author} is having an orgasm",
                "$GASM Having fun? *pats~* $PAT"
            )
        )

        //handjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("handjob"), "Handjob Command", "Gives a handjob to the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/handjob.txt"],
            ActionLines(
                "$FUCK {author} is giving {mentions} a handjob",
                "",
                "$FUCK W-What? Am I supposed to...? *gives you a handjob~* Lewdie~",
                "$FUCK Eeh..? What? Since when I have a dick?? That's lewd! B-but don't stop!"
            )
        )

        //handjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("handjobcum", "cumhandjob"), "Handjob Cum Command", "Cums while someone is doing you a handjob.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/handjobcum.txt"],
            ActionLines(
                "$FUCK {author} is giving {mentions} a handjob... $CUM",
                "",
                "$GASM S-something is c-coming out... $CUM",
                "$FUCK Could you like... not cum on my hand? Now lick it, you baka!"
            )
        )

        //lesbian
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("lesbian"), "Lesbian Command", "Lesbian fun, together.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/lesbian.txt"],
            ActionLines(
                "$FUCK {everyone} are having some decent fun together",
                "",
                "$FUCK *plays with you~* Lewdie~",
                "$FUCK *plays with you~* Lewdie~"
            )
        )

        //masturbate
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("masturbate"), "Masturbate Command", "When you're a girl and you're just way too bored.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/masturbating.txt"],
            ActionLines(
                "$GASM {mentions}, {author} is masturbating in front of you",
                "",
                "$GASM {author} is masturbating",
                "$GASM Having fun? *pats~* $PAT"
            )
        )

        //pussylick
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussylick"), "Pussylick Command", "Licks the pussy of the mentioned users", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussylick.txt"],
            ActionLines(
                "$FUCK {author} is licking {mentions}'s pussy",
                "",
                "$FUCK *Licks your pussy~* Lewdiee~",
                "$GASM You're good with the tongue, heh?"
            )
        )

        //pussyplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussyplay"), "Pussyplay Command", "Plays with the boobs of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussyplay.txt"],
            ActionLines(
                "$FUCK {author} is playing with {mentions}'s pussy",
                "",
                "$FUCK *Plays with your pussy~* Lewdiee~",
                "$FUCK Eeh..? W-Why are you playing with my pussy? That's lewd, baka!"
            )
        )

        //pussytease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussytease"), "Pussytease Command", "Teases the mentioned users using your pussy.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussytease.txt"],
            ActionLines(
                "$FUCK {author} is using her pussy to tease {mentions}",
                "",
                "$TEEHEE *Teases you~*",
                "$FUCK Eeh..? W-Why are you teasing me?"
            )
        )

        //spank
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("spank"), "Spank Command", "Spanks the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/spank.txt"],
            ActionLines(
                "$FUCK {author} is spanking {mentions}",
                "",
                "$FUCK *Spanks you~* BAKAA!",
                "$CRY Don't spank mee~ I did nothing wroong. $CRY"
            )
        )

        //stripclothes
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("stripclothes"), "Strip Clothes Command", "Strips the clothes of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/stripclothes.txt"],
            ActionLines(
                "$FUCK {author} is stripping {mentions}'s clothes",
                "",
                "$FUCK *strips your clothes~* Lewdiee~",
                "$FUCK D-DON'T... *gets clothes stripped~* Y-you bakaa!"
            )
        )

        //striptease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("striptease"), "Striptease Command", "Stripteases in front of the mentioned users.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/striptease.txt"],
            ActionLines(
                "$FUCK {mentions}, {author} is strip-teasing in front of you",
                "",
                "$FUCK {author} is stripteasing you",
                "$FUCK *stripteases in front of you~* I-I'm not doing this because you asked, b-baka."
            )
        )

        //tentacles
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("tentacles"), "Tentacles Command", "Tentacle fun.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/tentacles.txt"],
            ActionLines(
                "$FUCK {mentions}, you're getting fucked by {author}'s tentacles",
                "",
                "$FUCK {author} is getting fucked by tentacles",
                "$FUCK W-Wha..? *gets fucked by tentacles~* $CRY That's not funny..."
            )
        )

        //toying
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("toying"), "Toying Command", "When you're a girl and you're REALLY bored.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/toying.txt"],
            ActionLines(
                "$GASM {mentions}, {author} is toying herself in front of you",
                "",
                "$GASM {author} is toying herself",
                "$GASM Having fun? *pats~* $PAT"
            )
        )

        //weirdfuck
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("weirdfuck", "sillyfuck"), "Weird Fuck Command", "Fucks the mentioned users... in a very weird way.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/weirdfuck.txt"],
            ActionLines(
                "$FUCK {mentions}, {author} is fucking you in a... rather weird way",
                "",
                "$FUCK Eeh..? Since when I have a dick?? Am I supposed to...? *fucks you in a weird way~* I don't even know what am I doing.",
                "$FUCK Wha?? *gets confused while getting fucked~* What the fuck are you doing with me...?"
            )
        )
    }
}