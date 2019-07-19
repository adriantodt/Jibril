package pw.aru.sides

enum class AruSide(val moduleName: String) {
    MAIN(
        "pw.aru.mainbot"
    ),

    DEV(
        "pw.aru.devbot"
    ),

    PATREON(
        "pw.aru.patreonbot"
    ),

    WEB(
        "pw.aru.web"
    ),

    AUXILIARY(
        "pw.aru.auxiliary"
    )
}
