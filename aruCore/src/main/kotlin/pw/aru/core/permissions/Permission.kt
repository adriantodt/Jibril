package pw.aru.core.permissions

annotation class P(val value: String)

interface Permission {
    val name: String
    val description: String
}

@P("global")
enum class UserPermissions(override val description: String) : Permission {
    USE_BOT("Use the Bot"),         // You're not Blacklisted
    PREMIUM("Premium User"),        // You're Premium
    BOT_DEVELOPER("Bot Developer"), // Hi you're bot dev
}

@P("guild")
enum class MemberPermissions(override val description: String) : Permission {
    OWNER("Server Owner"),          // You literally own the server
    ADMIN("Server Admin"),          // Administrator (or has a ARU!ADMIN)
    SERVER("Manage Server"),        // You can manage the server

    DJ("DJ"),                       // You can manage music

    ROLES("Manage Roles"),          // You can manage roles
    CHANNELS("Manage Channels"),    // You can manage channels
    MESSAGES("Manage Messages"),    // You can manage messages
    NICKNAMES("Manage Nicknames"),  // You can manage nicknames

    KICK("Kick Members"),           // You can kick people
    BAN("Ban Members"),             // You can ban people
}
