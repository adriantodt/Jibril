package pw.aru.core.parser

import pw.aru.utils.Resource

internal fun Args.runOptions(list: List<Pair<(String) -> Boolean, Args.() -> Unit>>) {
    val map = list.toMap(LinkedHashMap())

    while (true) {
        val (key, value) = map.entries.firstOrNull { matchNextString(it.key) } ?: return
        map.remove(key)
        value()
    }
}

class OptionBuilder {
    internal val list = ArrayList<Pair<(String) -> Boolean, Args.() -> Unit>>()

    fun option(predicate: (String) -> Boolean, function: Args.() -> Unit) {
        list += predicate to function
    }

    fun option(key: String, function: Args.() -> Unit) {
        list += key::equals to function
    }
}

class OptionCreatorBuilder<T> {
    internal val builder = OptionBuilder()
    internal var creator: () -> T = { throw IllegalStateException() }

    fun <V> option(predicate: (String) -> Boolean, mapper: Args.() -> V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(predicate) { res.setResourceAvailable(mapper()) }
        return res
    }

    fun <V> option(key: String, mapper: Args.() -> V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(key) { res.setResourceAvailable(mapper()) }
        return res
    }

    fun creator(mapper: () -> T) {
        creator = mapper
    }
}

fun Args.parseOptions(options: OptionBuilder.() -> Unit) {
    runOptions(OptionBuilder().also(options).list)
}

fun <V> Args.parseAndCreate(options: OptionCreatorBuilder<V>.() -> Unit): V {
    val builder = OptionCreatorBuilder<V>().also(options)
    runOptions(builder.builder.list)
    return builder.creator()
}