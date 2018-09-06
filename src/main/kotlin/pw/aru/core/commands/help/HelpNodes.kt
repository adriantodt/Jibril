package pw.aru.core.commands.help

import pw.aru.Aru
import pw.aru.core.commands.CommandPermission
import java.awt.Color

sealed class BaseDescription

data class CommandDescription(
    val names: List<String>,
    val description: String,
    val permission: CommandPermission? = null,
    val color: Color? = null,
    val thumbnail: String = "https://i.imgur.com/uxHqhwt.png"
) : BaseDescription()

data class CategoryDescription(
    val description: String,
    val permission: CommandPermission? = null,
    val color: Color? = null,
    val thumbnail: String = "https://i.imgur.com/uxHqhwt.png"
) : BaseDescription()

//=== w ===//

sealed class HelpNode

data class Description(val value: String) : HelpNode() {
    constructor(vararg values: String) : this(values.joinToString("\n"))
}

data class Usage(val nodes: List<UsageNode>) : HelpNode() {
    constructor(vararg nodes: UsageNode) : this(nodes.toList())
}

data class Note(val value: String) : HelpNode() {
    constructor(vararg values: String) : this(values.joinToString("\n"))
}

data class SeeAlso(val value: String) : HelpNode() {
    constructor(vararg values: UsageNode) : this(values.joinToString("\n"))

    companion object {
        @JvmStatic
        @JvmName("simpleList")
        operator fun get(vararg values: String) = SeeAlso(values.joinToString("` `", "`", "`"))

        fun ofList(names: List<String>) = SeeAlso(names.joinToString("` `", "`", "`"))
    }
}

data class Example(val values: List<String>, val withPrefix: Boolean = true) : HelpNode() {
    constructor(vararg values: String, withPrefix: Boolean = true) : this(values.toList(), withPrefix)

    val displayValues: List<String>
        get() = if (withPrefix) values.map { "$prefix$it" } else values
}

data class Field(val name: String, val value: String) : HelpNode() {
    constructor(name: String, vararg values: String) : this(name, values.joinToString("\n"))
}

//=== w ===//

sealed class UsageNode

fun commandUsage(command: String, description: String) = "`$prefix$command` - $description"

fun commandUsage(command: String, extra: String, description: String) = "`$prefix$command` $extra - $description"

data class CommandUsage(val command: String, val extra: String?, val description: String) : UsageNode() {
    constructor(command: String, description: String) : this(command, null, description)

    override fun toString() = if (extra != null) commandUsage(command, extra, description) else commandUsage(command, description)
}

data class TextUsage(val value: String) : UsageNode() {
    override fun toString() = value
}

object UsageSeparator : UsageNode() {
    override fun toString() = ""
}

val prefix get() = Aru.prefixes.first()