package pw.aru.core.config

data class AruConfig(
    // dev mode
    var type: String = "dev",

    // discord token
    var botToken: String = "",

    // api tokens
    var wshToken: String = "",

    // logging
    var consoleWebhook: String = "",
    var serversWebhook: String = ""
)