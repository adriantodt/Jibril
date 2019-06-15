package pw.aru.core.parser

import pw.aru.libs.resources.Resource

internal fun Args.runOptions(list: List<Pair<(String) -> Boolean, Args.() -> Unit>>) {
    val map = list.toMap(LinkedHashMap())

    while (true) {
        val (key, value) = map.entries.firstOrNull { matchNextString(it.key) } ?: return
        map.remove(key)
        while (map.values.remove(value));
        value()
    }
}

class OptionBuilder {
    internal val list = ArrayList<Pair<(String) -> Boolean, Args.() -> Unit>>()

    fun option(predicate: (String) -> Boolean, function: Args.() -> Unit) {
        list += predicate to function
    }

    fun option(vararg keys: String, function: Args.() -> Unit) {
        for (key in keys) {
            list += key::equals to function
        }
    }
}

class OptionCreatorBuilder<T> {
    internal val builder = OptionBuilder()
    internal var creator: () -> T = { throw IllegalStateException() }

    fun <V> option(predicate: (String) -> Boolean, value: V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(predicate) { res.setResourceAvailable(value) }
        return res
    }

    fun <V> option(predicate: (String) -> Boolean, mapper: Args.() -> V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(predicate) { res.setResourceAvailable(mapper()) }
        return res
    }

    fun <V> option(vararg keys: String, value: V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(*keys) { res.setResourceAvailable(value) }
        return res
    }

    fun <V> option(vararg keys: String, mapper: Args.() -> V): Resource<V> {
        val res = Resource.settable<V>()
        res.setResourceUnavailable()
        builder.option(*keys) { res.setResourceAvailable(mapper()) }
        return res
    }

    fun creator(mapper: () -> T) {
        creator = mapper
    }
}

fun Args.parseOptions(options: OptionBuilder.() -> Unit) {
    runOptions(OptionBuilder().also(options).list)
}

fun <T> Args.parseAndCreate(options: OptionCreatorBuilder<T>.() -> Unit): T {
    val builder = OptionCreatorBuilder<T>().also(options)
    runOptions(builder.builder.list)
    return builder.creator()
}