package es.ehfacturas.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIKeyManager @Inject constructor() {

    private val proxyBaseUrl = "https://facturaapp-proxy.workers.dev"

    private var authToken: String? = null
    val isAuthenticated: Boolean get() = authToken != null

    private var directApiKey: String? = null
    private var directMode = false

    fun setDirectApiKey(key: String) {
        directApiKey = key
        directMode = true
    }

    suspend fun authenticate(purchaseToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("purchaseToken", purchaseToken)
                put("platform", "android")
            }
            val response = httpPost("$proxyBaseUrl/authenticate", body)
            authToken = response.optString("token", null)
            authToken != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun sendRequest(endpoint: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        if (directMode && directApiKey != null) {
            sendDirectRequest(endpoint, body)
        } else {
            sendProxiedRequest(endpoint, body)
        }
    }

    private fun sendProxiedRequest(endpoint: String, body: JSONObject): JSONObject {
        val token = authToken ?: throw IllegalStateException("No autenticado")
        return httpPost(
            url = "$proxyBaseUrl/$endpoint",
            body = body,
            headers = mapOf("Authorization" to "Bearer $token")
        )
    }

    private fun sendDirectRequest(endpoint: String, body: JSONObject): JSONObject {
        val apiKey = directApiKey ?: throw IllegalStateException("No hay API key")

        val (url, headers) = when (endpoint) {
            "claude" -> "https://api.anthropic.com/v1/messages" to mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to "2023-06-01"
            )
            "openai" -> "https://api.openai.com/v1/chat/completions" to mapOf(
                "Authorization" to "Bearer $apiKey"
            )
            else -> throw IllegalArgumentException("Endpoint desconocido: $endpoint")
        }

        return httpPost(url, body, headers)
    }

    private fun httpPost(
        url: String,
        body: JSONObject,
        headers: Map<String, String> = emptyMap()
    ): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
            connectTimeout = 30_000
            readTimeout = 60_000
            doOutput = true
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                throw IllegalStateException("HTTP $responseCode: $error")
            }

            return JSONObject(responseBody)
        } finally {
            connection.disconnect()
        }
    }
}
