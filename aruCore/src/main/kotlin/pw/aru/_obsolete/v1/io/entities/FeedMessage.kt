package pw.aru._obsolete.v1.io.entities

import org.json.JSONObject
import pw.aru.core.AruSide

data class FeedMessage(val source: AruSide, val type: String, val data: JSONObject)