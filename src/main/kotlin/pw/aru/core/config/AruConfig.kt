package pw.aru.core.config

data class AruConfig(
    // dev mode
    var type: String = "main",

    // discord token
    var botToken: String = "",


    var wshToken: String = "",

    // logging
    var consoleWebhook: String = "",
    var serversWebhook: String = ""
)