package pw.aru.core.parser

class OptionBuilder {
    internal val list = ArrayList<Pair<(String) -> Boolean, Args.() -> Unit>>()

    fun option(predicate: (String) -> Boolean, function: Args.() -> Unit) {
        list += predicate to function
    }

    fun option(key: String, function: Args.() -> Unit) {
        list += key::equals to function
    }
}

fun Args.parseOptions(options: OptionBuilder.() -> Unit) {
    val map = OptionBuilder().also(options).list.toMap(LinkedHashMap())

    while (true) {
        val (key, value) = map.entries.firstOrNull { matchNextString(it.key) } ?: continue
        map.remove(key)
        value()
    }
}