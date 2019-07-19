package pw.aru.core.logging

import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.message.MessageOptions
import java.util.regex.Pattern

open class DiscordLogger(val catnip: Catnip, val url: String) {
    companion object {
        private val PATTERN = Pattern.compile("https://discordapp\\.com/api/webhooks/(\\d+)/([\\w\\W]+)")
    }

    private val id: String
    private val token: String

    init {
        val m = PATTERN.matcher(url)
        check(m.find()) { "'$url' is not a valid Discord webhook address." }
        id = m.group(1)
        token = m.group(2)
    }

    fun embed(builder: EmbedBuilder.() -> Unit) {
        catnip.rest().webhook().executeWebhook(id, token, MessageOptions().embed(EmbedBuilder().also(builder).build()))
    }
}