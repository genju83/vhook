package io.github.genju83.vhook

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.javalin.Javalin
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.nio.charset.StandardCharsets
import java.util.*

fun main() {

    val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Javalin.log.debug(message)
            }
        }))
        .build()

    val app = Javalin.create().apply {
        if (System.getenv("DEBUG") == "true") {
            config.enableDevLogging()
        }
    }
    app.before { it.res.characterEncoding = StandardCharsets.UTF_8.name() }.start(8506)

    app.post("/api/v1/hooks") { ctx ->
        val json = JsonParser().parse(ctx.body()) as JsonObject
        val url = extractUrl(json)
        val newHeaders = extractHeaders(json)
        val newBody = extractBody(json)

        val response = httpClient.newCall(
            Request.Builder().url(url).headers(newHeaders).post(newBody).build()
        ).execute()

        ctx.status(response.code)
    }

    app.post("/api/v1/chains") { ctx ->
        val replacementMap = HashMap<String, String>()
        val jsonArray = JsonParser().parse(ctx.body()) as JsonArray

        var code = 0

        jsonArray.toList()
            .map { json -> json as JsonObject }
            .forEach { json ->
                val url = extractUrl(json) { replaceFragments(replacementMap, it) }
                val newHeaders = extractHeaders(json) { replaceFragments(replacementMap, it) }
                val newBody = extractBody(json) { replaceFragments(replacementMap, it) }

                val response = httpClient.newCall(
                    Request.Builder().url(url).headers(newHeaders).post(newBody).build()
                ).execute()

                response.body?.let {
                    Gson().fromJson(it.string(), MutableMap::class.java).forEach { entry ->
                        replacementMap[entry.key.toString()] = entry.value.toString()
                    }
                }

                code = if (code < response.code) {
                    response.code
                } else {
                    code
                }
            }

        ctx.status(code)
    }
}

private fun extractUrl(
    json: JsonObject,
    manipulate: ((String) -> String) = { it }
): String {
    return manipulate(json["url"].asString)
}

private fun extractBody(
    json: JsonObject,
    manipulate: ((String) -> String) = { it }
): RequestBody {
    val body = json["body"] as? JsonObject ?: JsonObject()
    return manipulate(body.toString()).toRequestBody("application/json".toMediaTypeOrNull())
}

private fun extractHeaders(
    json: JsonObject,
    manipulate: ((String) -> String) = { it }
): Headers {
    val headers = json["headers"] as? JsonObject ?: JsonObject()
    return headers.entrySet().fold(Headers.Builder(), { builder, entry ->
        builder.add(entry.key, manipulate(entry.value.asString))
    }).build()
}

private fun replaceFragments(replacementMap: HashMap<String, String>, input: String): String {
    return "<(.*?)>".toRegex().replace(input) {
        val trimmed = it.value.replace("<", "").replace(">", "")
        replacementMap[trimmed] ?: it.value
    }
}
