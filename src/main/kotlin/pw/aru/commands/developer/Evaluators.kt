package pw.aru.commands.developer

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import pw.aru.db.AruDB
import javax.script.ScriptEngine

object Evaluators {
    fun newStatelessEvaluatorsMap(shardManager: ShardManager, db: AruDB): Map<String, Evaluator> {
        return mapOf(
            "js" to JsEvaluator(shardManager, db),
            "bsh" to BshEvaluator(shardManager, db)
        )
    }

    fun newPersistentEvaluatorsMap(shardManager: ShardManager, db: AruDB): Map<String, PersistentEvaluator> {
        return mapOf(
            "js" to PersistentJsEvaluator(shardManager, db),
            "bsh" to PersistentBshEvaluator(shardManager, db)
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
