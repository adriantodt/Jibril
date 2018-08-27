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
import pw.aru.utils.extensions.capitalize
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
            CustomCommandInfo(listOf("boobtease"), "Boobtease Command", "Teases the mentioned users with your boobs.", nsfw = true),
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
            CustomCommandInfo(listOf("butttease"), "Butttease Command", "Teases the mentioned users with your butts.", nsfw = true),
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
            CustomCommandInfo(listOf("fuck"), "Fuck Command", "Fucks the mentioned users... Hey, that's lewd!", nsfw = true),
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
                "$FUCK I brought some friends. And apparently have a dick now, too. Oh, well.. *starts gangbanging you~*",
                "$FUCK Eeh..? Who's all these... guys? *gets gangbanged~* $CRY That's not funny..."
            )
        )

        //gasm
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("gasm", "orgasm"), "Orgasm Command", "When you're a girl and someone breaks your decency. That's messy... And lewd.\nCheck `${prefix}cum` too!", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/gasm.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO handjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("handjob"), "Handjob Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/handjob.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO handjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("handjobcum"), "Handjobcum Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/handjobcum.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO lesbian
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("lesbian"), "Lesbian Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/lesbian.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO masturbating
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("masturbating"), "Masturbating Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/masturbating.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO pussylick
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussylick"), "Pussylick Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussylick.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO pussyplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussyplay"), "Pussyplay Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussyplay.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO pussytease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("pussytease"), "Pussytease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/pussytease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO spank
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("spank"), "Spank Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/spank.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO stripclothes
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("stripclothes"), "Stripclothes Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/stripclothes.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO striptease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("striptease"), "Striptease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/striptease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO tentacles
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("tentacles"), "Tentacles Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/tentacles.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO toying
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("toying"), "Toying Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/toying.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO weirdfuck
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("weirdfuck"), "Weirdfuck Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/weirdfuck.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )
    }
}

fun main(args: Array<String>) {
    println(File("assets/aru/nsfw_actions").listFiles().joinToString("\n\n") {
        val name = it.nameWithoutExtension
        val path = it.path.replace('\\', '/')
        val cName = name.capitalize()
        """
        //TODO $name
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("$name"), "$cName Command", "TODO DOCS", nsfw = true),
            assetProvider["$path"],
            ActionLines(
                "$ FUCK {author} {mentions} TODO",
                "$ FUCK *TODO~*",
                "$ FUCK *TODO you~* Lewdie~",
                "$ FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )
        """.trimIndent()
    }
    )
}