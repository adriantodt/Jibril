package pw.aru.io.handler

import org.json.JSONObject
import pw.aru.io.entities.FeedMessage
import pw.aru.sides.AruSide

class FeedMessageHandler {
    private val targetSides = HashSet<AruSide>()
    private val targetTypes = HashSet<String>()

    fun forSides(vararg sides: AruSide) = apply {
        targetSides += sides
    }

    fun forTypes(vararg types: String) = apply {
        targetTypes += types
    }

    fun procedure(block: (JSONObject) -> Unit): (FeedMessage) -> Unit {
        return { (side, type, data) ->
            if ((targetSides.isEmpty() || side in targetSides) && (targetTypes.isEmpty() || type in targetTypes)) {
                block(data)
            }
        }
    }
}