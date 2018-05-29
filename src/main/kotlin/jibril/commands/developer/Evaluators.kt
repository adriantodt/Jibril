package jibril.commands.developer

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object Evaluators {
    val engine = ScriptEngineManager()

    fun newStatelessEvaluatorsMap(shardManager: ShardManager): Map<String, Evaluator> {
        return mapOf(
            "js" to JsEvaluator(shardManager),
            "bsh" to BshEvaluator(shardManager)
        )
    }

    fun newPersistentEvaluatorsMap(shardManager: ShardManager): Map<String, PersistentEvaluator> {
        return mapOf(
            "js" to PersistentJsEvaluator(shardManager),
            "bsh" to PersistentBshEvaluator(shardManager)
        ).configureIntegrations()
    }

    private fun Map<String, PersistentEvaluator>.configureIntegrations() = apply {
        val globals = Globals()
        globals["eval"] = this

        forEach { (_, e) -> e["globals"] = globals }
    }
}

interface Evaluator {
    operator fun invoke(event: GuildMessageReceivedEvent, code: String): Any?
}

interface PersistentEvaluator : Evaluator {
    operator fun get(key: String): Any?
    operator fun set(key: String, value: Any?)
}

class Globals : LinkedHashMap<String, Any?>() {
    override fun toString() = "Globals" + keys.toString()
}

operator fun ScriptEngine.set(key: String, value: Any?) = put(key, value)
