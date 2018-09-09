package pw.aru.commands.developer

import bsh.Interpreter
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.core.CommandRegistry
import pw.aru.db.AruDB
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

interface Evaluator {
    operator fun invoke(event: GuildMessageReceivedEvent, code: String): Any?
}

operator fun ScriptEngine.set(key: String, value: Any?) = put(key, value)

class JsEvaluator(private val shardManager: ShardManager, private val db: AruDB, private val registry: CommandRegistry) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = ScriptEngineManager().getEngineByName("nashorn")
        engine["shards"] = shardManager
        engine["db"] = db
        engine["registry"] = registry
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval("imports = new JavaImporter(java.util, java.io, java.net);\n(function() {\nwith(imports) {\n$code\n}\n})()")
    }
}

class BshEvaluator(private val shardManager: ShardManager, private val db: AruDB, private val registry: CommandRegistry) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = Interpreter()
        engine["shards"] = shardManager
        engine["db"] = db
        engine["registry"] = registry
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval("import *;\n$code")
    }
}