package pw.aru.io.dsl

import org.json.JSONObject
import pw.aru.io.CallHandler
import pw.aru.io.FeedConsumer
import pw.aru.io.entities.CallResponse
import pw.aru.io.entities.CommandCall
import pw.aru.io.entities.FeedMessage
import pw.aru.io.entities.toCallError
import pw.aru.sides.AruSide

class AruIOConfigurator {
    private val sidesToListen = HashSet<AruSide>()
    private val feedHandlers = HashSet<FeedConsumer>()
    private val commandHandlers = HashSet<CallHandler>()

    fun feed(side: AruSide? = null, type: String? = null, procedure: (JSONObject) -> Unit) {
        configureFeed {
            side?.let { sides(it) }
            type?.let { types(it) }
            onFeed(procedure)
        }
    }

    fun procedure(method: String, side: AruSide? = null, block: (JSONObject) -> Unit) {
        configureCommand {
            methods(method)
            side?.let { onlySides(it) }
            procedure(block)
        }
    }

    fun function(method: String, side: AruSide? = null, block: (JSONObject) -> JSONObject) {
        configureCommand {
            methods(method)
            side?.let { onlySides(it) }
            function(block)
        }
    }

    fun optionalFunction(method: String, side: AruSide? = null, block: (JSONObject) -> JSONObject?) {
        configureCommand {
            methods(method)
            side?.let { onlySides(it) }
            optionalFunction(block)
        }
    }


    fun configureFeed(block: FeedConfigurator.() -> Unit) {
        feedHandlers += FeedConfigurator().also(block).make()
    }

    fun configureCommand(block: CommandConfigurator.() -> Unit) {
        commandHandlers += CommandConfigurator().also(block).make()
    }

    inner class FeedConfigurator {
        private val targetSides = HashSet<AruSide>()
        private val targetTypes = HashSet<String>()
        private lateinit var procedure: (JSONObject) -> Unit

        fun sides(vararg sides: AruSide) {
            sidesToListen += sides
            targetSides += sides
        }

        fun types(vararg types: String) {
            targetTypes += types
        }

        fun onFeed(block: (JSONObject) -> Unit) {
            procedure = block
        }

        internal fun make(): (FeedMessage) -> Unit {
            val sides = targetSides
            val types = targetTypes
            val block = procedure
            return { (side, type, data) ->
                if ((sides.isEmpty() || side in sides) && (types.isEmpty() || type in types)) {
                    block(data)
                }
            }
        }
    }

    inner class CommandConfigurator {
        private val targetSides = HashSet<AruSide>()
        private val targetMethods = HashSet<String>()
        private lateinit var procedure: (CommandCall) -> CallResponse?

        fun onlySides(vararg sides: AruSide) {
            targetSides += sides
        }

        fun methods(vararg methods: String) {
            targetMethods += methods
        }

        fun procedure(block: (JSONObject) -> Unit) {
            procedure = {
                it.data.runCatching(block).fold({ CallResponse.Unit }, Throwable::toCallError)
            }
        }

        fun function(block: (JSONObject) -> JSONObject) {
            procedure = {
                it.data.runCatching(block).fold(CallResponse::Success, Throwable::toCallError)
            }
        }

        fun optionalFunction(block: (JSONObject) -> JSONObject?) {
            procedure = {
                it.data.runCatching(block).fold(CallResponse::Success, Throwable::toCallError)
            }
        }

        internal fun make(): (CommandCall) -> CallResponse? {
            val sides = targetSides
            val methods = targetMethods
            val block = procedure
            return {
                if ((sides.isEmpty() || it.source in sides) && it.method in methods) {
                    block(it)
                } else {
                    null
                }
            }
        }
    }

    internal operator fun component1() = sidesToListen
    internal operator fun component2() = feedHandlers
    internal operator fun component3() = commandHandlers
}