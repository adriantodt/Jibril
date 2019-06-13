package pw.aru.core.options

import com.mewna.catnip.entity.guild.Guild
import pw.aru.core.commands.context.ContextMember
import pw.aru.core.parser.Args
import pw.aru.db.AruDB

sealed class Option(val name: String) {
    abstract val description: String
    abstract val filter: (OptionContext) -> Boolean
}

open class OptionGroup(name: String) : Option(name) {
    override lateinit var description: String
    override var filter: (OptionContext) -> Boolean = { true }

    private val map = LinkedHashMap<String, Option>()

    fun group(name: String): OptionGroup {
        val g = map.computeIfAbsent(name, ::OptionGroup)
        if (g is OptionGroup) {
            return g
        }

        throw IllegalStateException("$name is already defined and it's not a group.")
    }

    fun group(name: String, block: OptionGroup.() -> Unit): OptionGroup {
        return group(name).also(block)
    }

    fun property(name: String): OptionProperty {
        val p = map.computeIfAbsent(name, ::OptionProperty)
        if (p is OptionProperty) {
            return p
        }

        throw IllegalStateException("$name is already defined and it's not a property.")
    }

    fun property(name: String, block: OptionProperty.() -> Unit): OptionProperty {
        return property(name).also(block)
    }

    fun function(name: String): OptionFunction {
        val p = map.computeIfAbsent(name, ::OptionProperty)
        if (p is OptionFunction) {
            return p
        }

        throw IllegalStateException("$name is already defined and it's not a function.")
    }

    fun function(name: String, block: OptionFunction.() -> Unit): OptionFunction {
        return function(name).also(block)
    }
}

class Options : OptionGroup("<root>") {
    fun user() = group("user")
    fun user(block: OptionGroup.() -> Unit) = group("user", block)
    fun server() = group("user")
    fun server(block: OptionGroup.() -> Unit) = group("user", block)
}

class OptionFunction(name: String) : Option(name) {
    override lateinit var description: String
    override var filter: (OptionContext) -> Boolean = { true }

    private lateinit var function: OptionContext.() -> Unit
}

class OptionProperty(name: String) : Option(name) {
    lateinit var propertyName: String
    override lateinit var description: String
    override var filter: (OptionContext) -> Boolean = { true }

    lateinit var getter: OptionContext.() -> String?

    lateinit var setter: OptionContext.() -> Unit
    lateinit var clear: OptionContext.() -> Unit

}

data class OptionContext(
    val author: ContextMember,
    val guild: Guild,
    val db: AruDB,
    val args: Args
)
