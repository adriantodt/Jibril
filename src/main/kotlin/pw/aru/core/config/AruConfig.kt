package pw.aru.core.config

data class AruConfig(
    // dev mode
    var dev: Boolean = false,

    // discord token
    var botToken: String = "",

    // botlists token
    var dblToken: String = "",
    var dpwToken: String = "",
    var wshToken: String = "",

    // logging
    var consoleWebhook: String = "",
    var serversWebhook: String = "",

    // prefixes
    var prefixes: String = ""
)