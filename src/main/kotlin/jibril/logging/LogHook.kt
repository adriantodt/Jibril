package jibril.logging

import jibril.logging.Level.*
import jibril.utils.extensions.embed
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder

enum class Level {
    ALL, TRACE, DEBUG, INFO, WARN, ERROR, NONE;

    fun isEnabled(logLevel: Level): Boolean {
        return logLevel >= this
    }
}

data class LogHookManager(
    val level: Level,
    val username: String,
    val avatarUrl: String
)

class LogHook(val manager: LogHookManager, private val link: String) {

    fun log(embed: MessageEmbed) {
        WebhookClientBuilder(link)
            .build()
            .use {
                it.send(
                    WebhookMessageBuilder()
                        .setUsername(manager.username)
                        .setAvatarUrl(manager.avatarUrl)
                        .addEmbeds(embed)
                        .build()
                )
            }
    }

    inline fun log(error: Level, embed: EmbedBuilder.() -> Unit) {
        if (manager.level.isEnabled(error)) {
            log(embed(init = embed))
        }
    }

    inline fun error(embed: EmbedBuilder.() -> Unit) = log(ERROR, embed)
    inline fun warn(embed: EmbedBuilder.() -> Unit) = log(WARN, embed)
    inline fun info(embed: EmbedBuilder.() -> Unit) = log(INFO, embed)
    inline fun debug(embed: EmbedBuilder.() -> Unit) = log(DEBUG, embed)
    inline fun trace(embed: EmbedBuilder.() -> Unit) = log(TRACE, embed)

}