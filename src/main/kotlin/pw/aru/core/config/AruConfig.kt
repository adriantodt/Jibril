package pw.aru.core.config

data class AruConfig(
    // dev mode
    var type: String = "aruInterface",

    // discord token
    var botToken: String = "",

    // botlists tokens
    var dblToken: String = "",
    var dpwToken: String = "",
    var bfdToken: String = "",
    var blsToken: String = "",
    var ddbToken: String = "",
    var dbl2Token: String = "",
    var bodToken: String = "",
    var dbwToken: String = "",
    var dbgToken: String = "",
    var dbbToken: String = "",
    var dbiToken: String = "",
    var dboToken: String = "",
    var delToken: String = "",
    var lboToken: String = "",

    // api tokens
    var wshToken: String = "",

    // logging
    var consoleWebhook: String = "",
    var serversWebhook: String = ""
)