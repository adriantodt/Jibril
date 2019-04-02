package pw.aru.io.entities

import org.json.JSONObject

sealed class CommandResult {
    object Timeout : CommandResult()

    object EmptyResponse : CommandResult()

    data class Error(val type: String, val message: String?, val data: JSONObject?) : CommandResult()

    data class Successful(val data: JSONObject) : CommandResult()
}