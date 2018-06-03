package pw.aru.data.config

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

data class Config(
    var dev: Boolean = false,

    var tokens: Tokens = Tokens(),
    var channels: Channels = Channels(),
    var webhooks: Webhooks = Webhooks(),
    var database: DbConfig = DbConfig(),
    var api: ApiConfig = ApiConfig(),

    var prefixes: MutableList<String> = LinkedList(),
    var developers: MutableList<String> = LinkedList(),
    var blacklist: MutableList<String> = LinkedList()
)

data class Channels(
    var guild: String? = null,
    var logging: String? = null
)

data class Webhooks(
    var serverLog: String? = null
)

data class Tokens(
    var discord: String? = null,
    var weebSh: String? = null,
    var discordBots: String? = null
)

data class DbConfig(
    var hostname: String = "localhost",
    var port: Int = 6379
)

data class ApiConfig(
    var https: Boolean = false,
    var enabled: Boolean = true,
    var hostname: String = "localhost",
    var port: Int = 8080,
    var token: String = ""
)

@get:JsonIgnore
val DbConfig.address: String
    get() = "redis://$hostname:$port"

@get:JsonIgnore
val ApiConfig.address: String
    get() = "${if (https) "https" else "http"}://$hostname:$port"