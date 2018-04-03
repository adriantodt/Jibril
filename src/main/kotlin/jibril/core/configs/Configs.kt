package jibril.core.configs

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

interface IConfigProvider {
    fun configs(): List<IConfig>
}

interface IConfig {
    val id: String
    val name: String
    val description: String
}

interface IConfigCompound : IConfig {
    val map: Map<String, IConfig>
}

abstract class Config(override val id: String, override val name: String, override val description: String) : IConfig {
    abstract fun get(event: GuildMessageReceivedEvent): Any
}

abstract class ValueConfig(id: String, name: String, description: String) : Config(id, name, description) {
    abstract fun set(event: GuildMessageReceivedEvent, input: String): Any
}

abstract class ListConfig(id: String, name: String, description: String) : Config(id, name, description) {
    abstract fun add(event: GuildMessageReceivedEvent, input: String): Any

    abstract fun remove(event: GuildMessageReceivedEvent, input: String): Any
}

abstract class MapConfig(id: String, name: String, description: String) : Config(id, name, description) {
    abstract fun put(event: GuildMessageReceivedEvent, key: String, value: String): Any

    abstract fun remove(event: GuildMessageReceivedEvent, key: String): Any
}

open class ConfigCompound(override val id: String, override val name: String, override val description: String) : IConfigCompound {

    override val map = LinkedHashMap<String, IConfig>()

    constructor(id: String, name: String, description: String, init: MutableMap<String, IConfig>.() -> Unit) : this(id, name, description) {
        @Suppress("LeakingThis")
        map.init()
    }
}