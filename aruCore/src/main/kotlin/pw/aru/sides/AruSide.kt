package pw.aru.sides

enum class AruSide(val moduleName: String) {
    MAIN(
        "mainbot"
    ),

    DEV(
        "devbot"
    ),

    PATREON(
        "patreonbot"
    ),

    WEB(
        "web"
    ),

    AUXILIARY(
        "auxiliary"
    ),

    @Deprecated("Deprecated side")
    SECONDARY(
        "secondary"
    )
}
