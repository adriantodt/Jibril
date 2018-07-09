package pw.aru.commands.developer

import bsh.Interpreter
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.db.AruDB
import javax.script.ScriptEngineManager

class JsEvaluator(
    private val shardManager: ShardManager,
    private val db: AruDB
) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = ScriptEngineManager().getEngineByName("nashorn")
        engine["shards"] = shardManager
        engine["db"] = db
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(
            arrayOf(
                "imports = new JavaImporter(java.util, java.io, java.net);",
                "(function() {",
                "with(imports) {",
                code,
                "}",
                "})()"
            ).joinToString("\n")
        )
    }
}

class PersistentJsEvaluator(
    private val shardManager: ShardManager,
    private val db: AruDB
) : PersistentEvaluator {
    private val engine = ScriptEngineManager().getEngineByName("nashorn")

    override fun get(key: String): Any? = engine[key]

    override fun set(key: String, value: Any?) {
        engine[key] = value
    }

    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        engine["shards"] = shardManager
        engine["db"] = db
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(code)
    }
}

class BshEvaluator(
    private val shardManager: ShardManager,
    private val db: AruDB
) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = Interpreter()
        engine["shards"] = shardManager
        engine["db"] = db
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(arrayOf("import *;", code).joinToString("\n"))
    }
}

class PersistentBshEvaluator(
    private val shardManager: ShardManager,
    private val db: AruDB
) : PersistentEvaluator {
    val engine = Interpreter()

    override fun get(key: String): Any? = engine[key]

    override fun set(key: String, value: Any?) {
        engine[key] = value
    }

    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        engine.eval("import *;")
        engine["shards"] = shardManager
        engine["db"] = db
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(code)
    }
}

/*

    init {
        evals["js"] = { event, code ->
            val nashorn = ScriptEngineManager().getEngineByName("nashorn")
            nashorn["db"] = Bot.db
            nashorn["jda"] = event.jda
            nashorn["event"] = event
            nashorn["guild"] = event.guild
            nashorn["channel"] = event.channel

            try {
                nashorn.eval(
                    arrayOf(
                        "imports = new JavaImporter(java.util, java.io, java.net);",
                        "(function() {",
                        "with(imports) {",
                        code,
                        "}",
                        "})()"
                    ).joinToString("\n")
                )
            } catch (e: Exception) {
                e
            }
        }

        evals["bsh"] = { event, code ->
            try {
                val bsh = Interpreter()
                bsh["db"] = Bot.db
                bsh["jda"] = event.jda
                bsh["event"] = event
                bsh["guild"] = event.guild
                bsh["channel"] = event.channel

                bsh.eval(arrayOf("import *;", code).joinToString("\n"))
            } catch (e: Exception) {
                e
            }
        }
    }
 */