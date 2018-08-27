package pw.aru.commands.actions

import okhttp3.OkHttpClient
import pw.aru.commands.actions.base.ActionLines
import pw.aru.commands.actions.base.CustomCommandInfo
import pw.aru.commands.actions.base.URLsActionCommand
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Category
import pw.aru.core.commands.CommandProvider
import pw.aru.core.commands.ICommandProvider
import pw.aru.utils.ReloadableListProvider
import pw.aru.utils.caches.URLCache
import pw.aru.utils.emotes.CUM
import pw.aru.utils.emotes.FUCK
import pw.aru.utils.emotes.GASM
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
            CustomCommandInfo(listOf("anal"), "Anal Command", "Fucks the ass of the mentioned users... Hey, that's lewd!", nsfw = true),
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
            CustomCommandInfo(listOf("analcum"), "Anal Cum Command", "Cums inside... Okay, that's enough. And lewd.", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/analcum.txt"],
            ActionLines(
                "$FUCK {author} is cumming inside {mentions}'s ass",
                "",
                "$FUCK *Cums in your ass~* Lewdie~",
                "$FUCK EEH? D-DON'T... Baka, I said I don't want to be filled up!"
            )
        )

        //analplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("analplay"), "Anal Play Command", "Plays with... Okay, that's enough. And lewd.", nsfw = true),
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
            CustomCommandInfo(listOf("blowjobcum"), "Blowjob Cum Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/blowjobcum.txt"],
            ActionLines(
                "$FUCK {author} is sucking {mentions}'s dick... $CUM",
                "",
                "$FUCK *Swallows~* Could you like... not cum while I'm sucking you, baka?",
                "$GASM S-something is c-coming out... $CUM"
            )
        )

        //TODO boobjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobjob"), "Boobjob Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobjob.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO boobjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobjobcum"), "Boobjobcum Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobjobcum.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO booblick
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("booblick"), "Booblick Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/booblick.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO boobplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobplay"), "Boobplay Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobplay.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO boobtease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("boobtease"), "Boobtease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/boobtease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO buttplay
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("buttplay"), "Buttplay Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/buttplay.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO butttease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("butttease"), "Butttease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/butttease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO cocktease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("cocktease"), "Cocktease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/cocktease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO cum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("cum"), "Cum Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/cum.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO feetjob
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("feetjob"), "Feetjob Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/feetjob.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO feetjobcum
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("feetjobcum"), "Feetjobcum Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/feetjobcum.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
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

        //TODO fucktease
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("fucktease"), "Fucktease Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/fucktease.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO gangbang
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("gangbang"), "Gangbang Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/gangbang.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
                "$FUCK *TODO you~* Lewdie~",
                "$FUCK Eeh..? That's lewd! Bakaaa!"
            )
        )

        //TODO gasm
        URLsActionCommand(
            category, r, cache,
            CustomCommandInfo(listOf("gasm"), "Gasm Command", "TODO DOCS", nsfw = true),
            assetProvider["assets/aru/nsfw_actions/gasm.txt"],
            ActionLines(
                "$FUCK {author} {mentions} TODO",
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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
                "$FUCK *TODO~*",
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