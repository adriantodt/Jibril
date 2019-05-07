package pw.aru.utils.extensions.lang

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

fun <T> HttpClient.send(
    bodyHandler: HttpResponse.BodyHandler<T>,
    block: HttpRequest.Builder.() -> Unit
): HttpResponse<T> {
    return send(HttpRequest.newBuilder().also(block).build(), bodyHandler)
}

fun <T> HttpClient.sendAsync(
    bodyHandler: HttpResponse.BodyHandler<T>,
    block: HttpRequest.Builder.() -> Unit
): CompletableFuture<HttpResponse<T>> {
    return sendAsync(HttpRequest.newBuilder().also(block).build(), bodyHandler)
}
