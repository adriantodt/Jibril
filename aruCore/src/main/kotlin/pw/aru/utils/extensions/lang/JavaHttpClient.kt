package pw.aru.utils.extensions.lang

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun <T> HttpClient.send(
    bodyHandler: HttpResponse.BodyHandler<T>,
    block: HttpRequest.Builder.() -> Unit
): HttpResponse<T> {
    return send(HttpRequest.newBuilder().also(block).build(), bodyHandler)
}
