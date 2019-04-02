package pw.aru.io.entities

import org.json.JSONObject
import pw.aru.sides.AruSide

data class CommandCall(
    val source: AruSide,
    val callId: String,
    val method: String,
    val data: JSONObject
)