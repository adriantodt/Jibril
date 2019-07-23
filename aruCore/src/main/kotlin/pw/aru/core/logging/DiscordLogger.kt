package pw.aru.core.logging

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.impl.EntityBuilder
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import pw.aru.utils.extensions.lang.sendAsync
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.discarding

open class DiscordLogger(val url: String) {
    companion object {
        private val client = HttpClient.newHttpClient()
        private val dummyBuilder = EntityBuilder(null)
    }

    fun embed(builder: EmbedBuilder.() -> Unit) {
        client.sendAsync(discarding()) {
            uri(URI.create(url))
            header("Content-Type", "application/json")
            POST(
                HttpRequest.BodyPublishers.ofString(
                    JsonObject().put(
                        "embeds",
                        JsonArray().add(dummyBuilder.embedToJson(EmbedBuilder().also(builder).build()))
                    ).encode()
                )
            )
        }
    }

    fun text(vararg value: String) {
        client.sendAsync(discarding()) {
            uri(URI.create(url))
            header("Content-Type", "application/json")
            POST(
                HttpRequest.BodyPublishers.ofString(
                    JsonObject().put("content", value.joinToString("\n")).encode()
                )
            )
        }
    }
}