package pw.aru.io.handler

import org.json.JSONObject
import pw.aru.io.entities.CallResponse
import pw.aru.io.entities.CommandCall
import pw.aru.io.entities.toCallError
import pw.aru.sides.AruSide

class CommandCallHandler {
    private val targetSides = HashSet<AruSide>()
    private val targetMethods = HashSet<String>()

    fun forSides(vararg sides: AruSide) = apply {
        targetSides += sides
    }

    fun forMethods(vararg methods: String) = apply {
        targetMethods += methods
    }

    fun procedure(block: (JSONObject) -> Unit): (CommandCall) -> CallResponse? {
        return rawFunction {
            it.data.runCatching(block).fold({ CallResponse.Unit }, Throwable::toCallError)
        }
    }

    fun function(block: (JSONObject) -> JSONObject): (CommandCall) -> CallResponse? {
        return rawFunction {
            it.data.runCatching(block).fold(CallResponse::Success, Throwable::toCallError)
        }
    }

    fun optionalFunction(block: (JSONObject) -> JSONObject?): (CommandCall) -> CallResponse? {
        return rawFunction {
            it.data.runCatching(block).fold(CallResponse::Success, Throwable::toCallError)
        }
    }

    fun rawFunction(block: (CommandCall) -> CallResponse?): (CommandCall) -> CallResponse? {
        return {
            if ((targetSides.isEmpty() || it.source in targetSides) && it.method in targetMethods) {
                block(it)
            } else {
                null
            }
        }
    }
}