package pw.aru.commands.moderation

import mu.KLogging
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.CommandRegistry
import pw.aru.core.categories.Categories
import pw.aru.core.categories.Category
import pw.aru.core.commands.Command
import pw.aru.core.commands.CommandPermission
import pw.aru.core.commands.ICommand
import pw.aru.core.configs.*
import pw.aru.utils.J
import pw.aru.utils.emotes.ERROR
import pw.aru.utils.emotes.SUCCESS
import pw.aru.utils.extensions.*
import kotlin.collections.set

@Command("configs", "config", "confs", "conf")
class Configs : ICommand, ICommand.HelpHandler, ICommand.PostLoad, ICommand.Permission, IConfigProvider, IConfigCompound {

    companion object : KLogging()

    override val category: Category = Categories.MODERATION
    override val permission: CommandPermission = CommandPermission.SERVER_ADMIN

    override val id = "root"
    override val name = "Configs"
    override val description = "Tune and configure the bot here."

    override val map: MutableMap<String, IConfig> = LinkedHashMap()

    override fun call(event: GuildMessageReceivedEvent, args: String) {
        resolve(event, this, args)
    }

    override fun onHelp(event: GuildMessageReceivedEvent) {
        sendHelp(event, this)
    }

    private fun resolve(event: GuildMessageReceivedEvent, compound: IConfigCompound, args: String) {
        if (args.isEmpty()) return sendHelp(event, compound)

        val (key, value) = with(args.split(" ", limit = 2)) { get(0) to getOrElse(1) { "" } }

        val config = compound.map[key] ?: return sendHelp(event, compound)

        when (config) {
            is IConfigCompound -> {
                resolve(event, config, value)
            }
            is ValueConfig -> {
                if (value.isEmpty()) {
                    sendHelp(event, config)
                } else {
                    try {
                        setVal(event, config, value)
                    } catch (_: CommandExceptions.ShowHelp) {
                        sendHelp(event, config)
                    }
                }
            }
            is ListConfig -> {
                if (value.isEmpty()) {
                    sendHelp(event, config)
                } else {
                    val (k, v) = with(value.split(" ", limit = 2)) { get(0) to getOrNull(1) }
                    if (v == null) {
                        sendHelp(event, config)
                        return
                    }

                    when (k) {
                        "add" -> {
                            try {
                                addVal(event, config, v)
                            } catch (_: CommandExceptions.ShowHelp) {
                                sendHelp(event, config)
                            }
                        }
                        "remove" -> {
                            try {
                                remVal(event, config, v)
                            } catch (_: CommandExceptions.ShowHelp) {
                                sendHelp(event, config)
                            }
                        }
                        else -> {
                            sendHelp(event, config)
                        }
                    }
                }
            }
            is MapConfig -> {
                if (value.isEmpty()) {
                    sendHelp(event, config)
                } else {
                    val (f, k, v) = with(value.split(" ", limit = 3)) { arrayOf(get(0), getOrNull(1), getOrNull(2)) }
                    if (k == null) {
                        sendHelp(event, config)
                        return
                    }

                    when (f) {
                        "add" -> {
                            if (v == null) {
                                sendHelp(event, config)
                                return
                            }

                            try {
                                putVal(event, config, k, v)
                            } catch (_: CommandExceptions.ShowHelp) {
                                sendHelp(event, config)
                            }
                        }
                        "remove" -> {
                            try {
                                remVal(event, config, k)
                            } catch (_: CommandExceptions.ShowHelp) {
                                sendHelp(event, config)
                            }
                        }
                        else -> {
                            sendHelp(event, config)
                        }
                    }
                }
            }
            else -> IllegalStateException("config $key from $compound is ${config.javaClass.name}")
        }
    }

    private fun putVal(event: GuildMessageReceivedEvent, config: MapConfig, key: String, value: String) {
        try {
            val result = config.put(event, key, value)
            event.channel.sendMessage(
                "$SUCCESS Set `$key` on ${config.name} set to $result!"
            ).queue()
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(
                "$ERROR ${e.message}"
            ).queue()
        }
    }

    private fun remVal(event: GuildMessageReceivedEvent, config: MapConfig, key: String) {
        try {
            val result = config.remove(event, key)
            event.channel.sendMessage(
                "$SUCCESS Removed $result from ${config.name}!"
            ).queue()
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(
                "$ERROR ${e.message}"
            ).queue()
        }
    }

    private fun addVal(event: GuildMessageReceivedEvent, config: ListConfig, value: String) {
        try {
            val result = config.add(event, value)
            event.channel.sendMessage(
                "$SUCCESS ${config.name} set to $result!"
            ).queue()
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(
                "$ERROR ${e.message}"
            ).queue()
        }
    }

    private fun remVal(event: GuildMessageReceivedEvent, config: ListConfig, value: String) {
        try {
            val result = config.remove(event, value)
            event.channel.sendMessage(
                "$SUCCESS Removed $result from ${config.name}!"
            ).queue()
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(
                "$ERROR ${e.message}"
            ).queue()
        }
    }

    private fun setVal(event: GuildMessageReceivedEvent, config: ValueConfig, value: String) {
        try {
            val result = config.set(event, value)
            event.channel.sendMessage(
                "$SUCCESS ${config.name} set to $result!"
            ).queue()
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(
                "$ERROR ${e.message}"
            ).queue()
        }
    }

    private fun sendHelp(event: GuildMessageReceivedEvent, compound: IConfigCompound) {
        embed {
            helpEmbed(event, compound.name, permission)
            field("Description:", compound.description)
            field(
                "Options:",
                if (compound.map.keys.isEmpty()) "No options available."
                else compound.map.keys.sorted().joinToString("` `", "`", "`")
            )
        }.send(event).queue()
    }

    private fun sendHelp(event: GuildMessageReceivedEvent, config: ValueConfig) {
        embed {
            helpEmbed(event, config.name)
            field("Description:", config.description)
            field("Value:", J.toString(config.get(event)))
        }.send(event).queue()
    }

    private fun sendHelp(event: GuildMessageReceivedEvent, config: ListConfig) {
        embed {
            helpEmbed(event, config.name)
            field("Description:", config.description)
            field("Values:", J.toString(config.get(event)))
        }.send(event).queue()
    }

    private fun sendHelp(event: GuildMessageReceivedEvent, config: MapConfig) {
        embed {
            helpEmbed(event, config.name)
            field("Description:", config.description)
            field("Values:", J.toString(config.get(event)))
        }.send(event).queue()
    }

    override fun postLoad() {
        CommandRegistry.lookup.keys.forEach {
            if (it is IConfigProvider) it.configs().forEach { map[it.id] = it }
        }

        logger.info { "Finished! ${map.size} configs loaded!" }
    }

    override fun configs(): List<IConfig> = listOf(GuildConfigs)
}