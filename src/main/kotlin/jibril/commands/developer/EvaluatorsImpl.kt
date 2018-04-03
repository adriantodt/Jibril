package jibril.commands.developer

import bsh.Interpreter
import com.google.inject.Injector
import jibril.Jibril
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.script.ScriptEngineManager

class JsEvaluator(
    private val injector: Injector,
    private val shardManager: ShardManager
) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = ScriptEngineManager().getEngineByName("nashorn")
        engine["injector"] = injector
        engine["shards"] = shardManager
        engine["db"] = Jibril.db
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
    injector: Injector,
    shardManager: ShardManager
) : PersistentEvaluator {
    private val engine = ScriptEngineManager().getEngineByName("nashorn")

    init {
        engine["injector"] = injector
        engine["shards"] = shardManager
        engine["db"] = Jibril.db
    }

    override fun get(key: String): Any? = engine[key]

    override fun set(key: String, value: Any?) {
        engine[key] = value
    }

    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(code)
    }
}

class BshEvaluator(
    private val injector: Injector,
    private val shardManager: ShardManager
) : Evaluator {
    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
        val engine = Interpreter()
        engine["injector"] = injector
        engine["shards"] = shardManager
        engine["db"] = Jibril.db
        engine["jda"] = event.jda
        engine["event"] = event
        engine["guild"] = event.guild
        engine["channel"] = event.channel

        return engine.eval(arrayOf("import *;", code).joinToString("\n"))
    }
}

class PersistentBshEvaluator(
    injector: Injector,
    shardManager: ShardManager
) : PersistentEvaluator {
    val engine = Interpreter()

    init {
        engine.eval("import *;")
        engine["injector"] = injector
        engine["shards"] = shardManager
    }

    override fun get(key: String): Any? = engine[key]

    override fun set(key: String, value: Any?) {
        engine[key] = value
    }

    override fun invoke(event: GuildMessageReceivedEvent, code: String): Any? {
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