package pw.aru.utils.extensions.lib.integration

import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject

fun ResponseBody.jsonObject() = JSONObject(string())

fun ResponseBody.jsonArray() = JSONArray(string())