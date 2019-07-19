package pw.aru.io.entities

import org.json.JSONObject
import pw.aru.utils.extensions.lang.especializationName
import pw.aru.utils.extensions.lib.jsonOf

sealed class CallResponse {
    open class Success(val data: JSONObject?) : CallResponse()
    class Error(val type: String, val message: String?, val data: JSONObject?) : CallResponse()
    object Unit : CallResponse.Success(null)
}

class CommandCallException(type: String, message: String?, data: JSONObject? = null) : Exception(message) {
    val response = CallResponse.Error(type, message, data)
}

fun Throwable.toCallError(): CallResponse.Error {
    return (this as? CommandCallException)?.response
        ?: CallResponse.Error(especializationName().decapitalize(), message, jsonOf("generated" to true))
}