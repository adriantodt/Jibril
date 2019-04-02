package pw.aru.commands.developer

import com.github.natanbc.weeb4j.Account
import com.github.natanbc.weeb4j.Weeb4J
import com.github.natanbc.weeb4j.image.FileType
import com.github.natanbc.weeb4j.image.HiddenMode
import com.github.natanbc.weeb4j.image.NsfwFilter
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.parser.Args
import pw.aru.core.parser.parseAndCreate
import pw.aru.utils.extensions.lang.invoke
import pw.aru.utils.extensions.lib.description
import pw.aru.utils.styling
import pw.aru.utils.text.CONFUSED

class DevWeebSh(private val weebSh: Weeb4J) {

    operator fun invoke(context: CommandContext, args: Args) {
        context.weebsh(args)
    }

    private fun CommandContext.weebsh(args: Args) {
        if (args.matchNextString("-account")) {
            return weebshAccount()
        }

        val image = args.parseAndCreate<GetImage> {
            val type = option("-type") { takeString() }
            val tags = option("-tags") { takeString().split(',') }
            val ext = option("-ext") { FileType.valueOf(takeString().toUpperCase()) }
            val hidden = option("-hidden") { HiddenMode.valueOf(takeString().toUpperCase()) }
            val nsfw = option("-nsfw") { NsfwFilter.valueOf(("${takeString()}_NSFW").toUpperCase()) }

            creator {
                GetImage(
                    type.orNull,
                    tags.orNull,
                    ext.orNull,
                    hidden.orNull ?: HiddenMode.DEFAULT,
                    nsfw.orNull ?: NsfwFilter.NO_NSFW
                )
            }
        }

        if (image.isNotEmpty()) {
            return weebshGet(image)
        }

        val imageTypes = weebSh.imageProvider.imageTypes.submit()
        val imageTags = weebSh.imageProvider.imageTags.submit()

        sendEmbed {
            styling(message)
                .author("Aru! | Weeb.sh Debug")
                .applyAll()

            thumbnail("https://assets.aru.pw/img/yes.png")

            description(
                "Types:",
                "```",
                imageTypes().types.sorted().joinToString(" "),
                "```",
                "",
                "Tags:",
                "```",
                imageTags().sorted().joinToString(" "),
                "```"
            )
        }
    }

    private fun CommandContext.weebshGet(img: GetImage) {
        weebSh.imageProvider.getRandomImage(img.type, img.tags, img.hidden, img.nsfw, img.ext).async { image ->
            if (image == null) {
                send("$CONFUSED No images found... ")
            } else {
                sendEmbed {
                    styling(message)
                        .author("Aru! | Weeb.sh Debug")
                        .applyAll()

                    image(image.url)
                    description(
                        "Type: ${image.type}",
                        "Tags: ${image.tags.joinToString(", ", "[", "]") { "Tag[name=${it.name}, user=${it.user}]" }}",
                        "Account: ${image.account}"
                    )
                }
            }
        }
    }

    private fun CommandContext.weebshAccount() {
        sendEmbed {
            styling(message)
                .author("Aru! | Weeb.sh Debug")
                .applyAll()

            thumbnail("https://assets.aru.pw/img/yes.png")

            val (id, name, discordId, active, scopes) = weebSh.tokenInfo.execute().account

            description(
                "ID: ``$id``",
                "Name: ``$name``",
                "DiscordID: ``$discordId``",
                "Active: ${active.toString().toLowerCase().capitalize()}",
                "",
                "Scopes:",
                "```",
                scopes.sorted().joinToString(" "),
                "```"
            )
        }
    }

    private operator fun Account.component1() = id
    private operator fun Account.component2() = name
    private operator fun Account.component3() = discordId
    private operator fun Account.component4() = isActive
    private operator fun Account.component5() = scopes

    data class GetImage(
        val type: String?,
        val tags: List<String>?,
        val ext: FileType?,
        val hidden: HiddenMode = HiddenMode.DEFAULT,
        val nsfw: NsfwFilter = NsfwFilter.NO_NSFW
    ) {
        fun isNotEmpty() = (type != null && type.isNotEmpty()) || (tags != null && tags.isNotEmpty())
    }
}