package pw.aru.data.config

data class AruConfig(
    // dev mode
    var dev: Boolean = false,

    // discord token
    var botToken: String = "",

    // botlists token
    var dblToken: String = "",
    var dpwToken: String = "",

    // logging
    var logChannel: String = "",
    var serverWebhook: String = "",

    // prefixes
    var prefixes: String = ""
)