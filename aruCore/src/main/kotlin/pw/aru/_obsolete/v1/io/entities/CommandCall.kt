package pw.aru._obsolete.v1.io.entities

import org.json.JSONObject
import pw.aru.core.AruSide

data class CommandCall(
    val source: AruSide,
    val callId: String,
    val method: String,
    val data: JSONObject
)