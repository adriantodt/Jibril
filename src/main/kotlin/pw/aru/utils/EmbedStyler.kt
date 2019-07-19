package pw.aru.utils

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.message.Message
import pw.aru.utils.extensions.lang.applyOn

class EmbedStyler(private val builder: EmbedBuilder, private val message: Message) {
    fun aruColor() = applyOn(builder) {
        color(AruColors.primary)
    }

    fun autoFooter() = applyOn(builder) {
        footer(
            "Requested by ${message.member()!!.effectiveName()}",
            message.author().effectiveAvatarUrl()
        )
    }

    fun author(
        name: String,
        url: String? = null,
        image: String? = message.catnip().selfUser()?.effectiveAvatarUrl()
    ) = applyOn(builder) {
        author(name, url, image)
    }


    fun applyAll() = this
        .aruColor()
        .autoFooter()
}

fun EmbedBuilder.styling(message: Message) = EmbedStyler(this, message)