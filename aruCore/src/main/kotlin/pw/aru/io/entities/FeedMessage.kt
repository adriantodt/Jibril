package pw.aru.io.entities

import org.json.JSONObject
import pw.aru.sides.AruSide

data class FeedMessage(val source: AruSide, val type: String, val data: JSONObject)