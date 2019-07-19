package pw.aru.utils.extensions.lib.integration

import okhttp3.ResponseBody
import pw.aru.utils.extensions.lib.toJsonArray
import pw.aru.utils.extensions.lib.toJsonObject

fun ResponseBody.jsonObject() = string().toJsonObject()

fun ResponseBody.jsonArray() = string().toJsonArray()