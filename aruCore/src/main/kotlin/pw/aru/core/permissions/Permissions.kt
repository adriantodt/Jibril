package pw.aru.core.permissions

abstract class Permissions {
    class Just(val p: Permission) : Permissions() {
        override fun check(permission: Collection<Permission>) = permission.contains(p)

        override fun toString() = "**${p.description}**"
    }

    class AnyOf(vararg permission: Permission) : Permissions() {
        val p = permission.toSet()

        override fun check(permission: Collection<Permission>): Boolean {
            return permission.any(p::contains)
        }

        override fun toString(): String {
            return when (p.size) {
                0 -> "anything (empty)"
                1 -> "**${p.first().description}**"
                2 -> "**${p.first().description}** or **${p.last().description}**"
                else -> {
                    val list = p.mapTo(ArrayList()) { "**${it.description}**" }
                    val last = list.removeAt(list.lastIndex)
                    list.joinToString(", ", postfix = " or $last")
                }
            }
        }
    }

    class AllOf(vararg permission: Permission) : Permissions() {
        val p = permission.toSet()

        override fun check(permission: Collection<Permission>): Boolean {
            return permission.containsAll(p)
        }

        override fun toString(): String {
            return when (p.size) {
                0 -> "anything (empty)"
                1 -> "**${p.first().description}**"
                2 -> "**${p.first().description}** and **${p.last().description}**"
                else -> {
                    val list = p.mapTo(ArrayList()) { "**${it.description}**" }
                    val last = list.removeAt(list.lastIndex)
                    list.joinToString(", ", postfix = " and $last")
                }
            }
        }
    }

    class AnyOfMulti(vararg permissions: Permissions) : Permissions() {
        val p = permissions.toSet()

        override fun check(permission: Collection<Permission>): Boolean {
            return p.any { it.check(permission) }
        }

        override fun toString(): String {
            return when (p.size) {
                0 -> "anything (empty)"
                1 -> p.first().toString()
                2 -> "(${p.first()}) or (${p.last()})"
                else -> {
                    val list = p.mapTo(ArrayList(), Permissions::toString)
                    val last = list.removeAt(list.lastIndex)
                    list.joinToString(prefix = "(", separator = "), (", postfix = ") or ($last)")
                }
            }
        }
    }


    class AllOfMulti(vararg permissions: Permissions) : Permissions() {
        val p = permissions.toSet()

        override fun check(permission: Collection<Permission>): Boolean {
            return p.all { it.check(permission) }
        }

        override fun toString(): String {
            return when (p.size) {
                0 -> "anything (empty)"
                1 -> p.first().toString()
                2 -> "(${p.first()}) and (${p.last()})"
                else -> {
                    val list = p.mapTo(ArrayList(), Permissions::toString)
                    val last = list.removeAt(list.lastIndex)
                    list.joinToString(prefix = "(", separator = "), (", postfix = ") and ($last)")
                }
            }
        }
    }

    object None : Permissions() {
        override fun check(permission: Collection<Permission>) = true

        override fun toString() = "anything"
    }

    abstract fun check(permission: Collection<Permission>): Boolean
}