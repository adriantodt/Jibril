package pw.aru.commands.developer

import bsh.Interpreter
import com.mewna.catnip.entity.message.Message
import pw.aru.AruBot
import pw.aru.core.CommandRegistry
import pw.aru.core.commands.context.CommandContext
import pw.aru.core.parser.Args
import pw.aru.core.reporting.LocalPastes
import pw.aru.db.AruDB
import pw.aru.utils.Colors
import pw.aru.utils.EmbedFirst
import pw.aru.utils.extensions.lang.limit
import pw.aru.utils.extensions.lang.random
import pw.aru.utils.extensions.lang.stackTraceToString
import pw.aru.utils.extensions.lang.toPrettyString
import pw.aru.utils.extensions.lib.field
import pw.aru.utils.styling
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


class DevEval(db: AruDB, registry: CommandRegistry) {

    operator fun invoke(context: CommandContext, args: Args) {
        context.eval(args.takeRemaining())
    }

    private val evals: Map<String, Evaluator> = mapOf(
        "js" to JsEvaluator(db, registry),
        "bsh" to BshEvaluator(db, registry)
    )

    private fun CommandContext.listEvals() {
        sendEmbed {
            styling(message)
                .author("DevConsole | Available Evaluators")
                .applyAll()

            description(evals.entries.joinToString("\n\n") { (k, v) -> "``$k`` - ${v.javaClass.simpleName}" })
        }
    }

    private fun CommandContext.eval(args: String) {
        val (eval, code) = args.split(" ", limit = 2).run { get(0) to getOrElse(1) { "" } }
        if (eval.isEmpty()) return listEvals()

        val evaluator = evals[eval] ?: return showHelp()

        EmbedFirst(message) {
            styling(message)
                .author("DevConsole | Evaluating...")
                .autoFooter()

            color(Colors.blurple)
            thumbnail("https://assets.aru.pw/img/loading.gif")
            description("*${AruBot.evaluatingQuotes.random()}*")
        } then {
            kotlin.runCatching {
                evaluator(message, code)
            }.onFailure { e ->
                styling(message).author("DevConsole | Evaluated and errored")
                color(Colors.discordRed)
                thumbnail("https://assets.aru.pw/img/no.png")
                description("")
                field(
                    e.javaClass.name,
                    e.message!!.limit(1024)
                )
                field("Full Stacktrace:", LocalPastes.paste("Full Stacktrace:", e.stackTraceToString()), "java")
            }.onSuccess { result ->
                styling(message).author("DevConsole | Evaluated with success")
                color(Colors.discordGreen)
                thumbnail("https://assets.aru.pw/img/yes.png")
                description(
                    "Evaluated with success ${if (result == null) "with no objects returned." else "and returned an object."}"
                )
                if (result != null) {
                    val toString = result.toPrettyString()
                    field(
                        result.javaClass.simpleName,
                        toString.limit(1024)
                    )

                    if (toString.length > 1024) {
                        field("Full ToString:", LocalPastes.paste("Full toString():", toString))
                    }
                }
            }

            footer("Evaluated by ${author.effectiveName()}", author.effectiveAvatarUrl())
        }
    }

    interface Evaluator {
        operator fun invoke(message: Message, code: String): Any?
    }

    class JsEvaluator(private val db: AruDB, private val registry: CommandRegistry) : Evaluator {
        override fun invoke(message: Message, code: String): Any? {
            val engine: ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")
            engine["db"] = db
            engine["registry"] = registry
            engine["catnip"] = message.catnip()
            engine["message"] = message
            engine["guild"] = message.guild()
            engine["channel"] = message.channel()

            return engine.eval("imports = new JavaImporter(java.util, java.io, java.net);\n(function() {\nwith(imports) {\n$code\n}\n})()")
        }

        private operator fun ScriptEngine.set(key: String, value: Any?) = put(key, value)
    }

    class BshEvaluator(private val db: AruDB, private val registry: CommandRegistry) : Evaluator {
        override fun invoke(message: Message, code: String): Any? {
            val engine = Interpreter()
            engine["db"] = db
            engine["registry"] = registry
            engine["catnip"] = message.catnip()
            engine["message"] = message
            engine["guild"] = message.guild()
            engine["channel"] = message.channel()

            return engine.eval("import *;\n$code")
        }
    }
}