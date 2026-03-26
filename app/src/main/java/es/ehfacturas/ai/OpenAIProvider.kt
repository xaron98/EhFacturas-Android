package es.ehfacturas.ai

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class OpenAIProvider @Inject constructor(
    private val apiKeyManager: APIKeyManager
) : AIProvider {

    companion object {
        private const val MODEL = "gpt-4o-mini"
        private const val MAX_TOOL_ITERATIONS = 3
    }

    override val tipo = AIProviderType.OPENAI
    override val isAvailable: Boolean get() = apiKeyManager.isAuthenticated || true
    override val unavailableReason = "Se necesita suscripción Pro para usar OpenAI"

    private val conversationHistory = mutableListOf<JSONObject>()

    override suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        // Asegurar system prompt al inicio
        if (conversationHistory.isEmpty()) {
            conversationHistory.add(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
        }

        // Añadir mensaje del usuario
        conversationHistory.add(JSONObject().apply {
            put("role", "user")
            put("content", texto)
        })

        var lastToolUsed: String? = null
        var iterations = 0

        // Convertir tools al formato OpenAI
        val openaiTools = if (tools.isNotEmpty()) {
            JSONArray(tools.map { tool ->
                JSONObject().apply {
                    put("type", "function")
                    put("function", JSONObject().apply {
                        put("name", (tool["name"] as? String) ?: "")
                        put("description", (tool["description"] as? String) ?: "")
                        val inputSchema = tool["input_schema"] ?: tool["parameters"]
                        if (inputSchema is Map<*, *>) {
                            put("parameters", mapToJson(inputSchema as Map<String, Any>))
                        }
                    })
                }
            })
        } else null

        while (iterations < MAX_TOOL_ITERATIONS) {
            iterations++

            val body = JSONObject().apply {
                put("model", MODEL)
                put("messages", JSONArray(conversationHistory.map { JSONObject(it.toString()) }))
                openaiTools?.let { put("tools", it) }
            }

            val response = apiKeyManager.sendRequest("openai", body)

            val choices = response.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            val finishReason = choices.getJSONObject(0).optString("finish_reason", "stop")

            // Añadir respuesta a historial
            conversationHistory.add(JSONObject(message.toString()))

            if (finishReason == "tool_calls" && message.has("tool_calls")) {
                val toolCalls = message.getJSONArray("tool_calls")

                for (i in 0 until toolCalls.length()) {
                    val toolCall = toolCalls.getJSONObject(i)
                    val function = toolCall.getJSONObject("function")
                    val toolName = function.getString("name")
                    val toolId = toolCall.getString("id")

                    val arguments = try {
                        jsonObjectToMap(JSONObject(function.getString("arguments")))
                    } catch (e: Exception) {
                        emptyMap()
                    }

                    lastToolUsed = toolName
                    val result = onToolCall(toolName, arguments)

                    // Añadir resultado de tool
                    conversationHistory.add(JSONObject().apply {
                        put("role", "tool")
                        put("tool_call_id", toolId)
                        put("content", result)
                    })
                }
            } else {
                // Respuesta final
                val textoRespuesta = message.optString("content", "")
                return AICommandResult(
                    texto = textoRespuesta,
                    toolUsed = lastToolUsed
                )
            }
        }

        return AICommandResult(
            texto = "Se alcanzó el límite de iteraciones.",
            toolUsed = lastToolUsed
        )
    }

    override fun resetSession() {
        conversationHistory.clear()
    }

    private fun mapToJson(map: Map<String, Any>): JSONObject {
        val json = JSONObject()
        map.forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> json.put(key, mapToJson(value as Map<String, Any>))
                is List<*> -> json.put(key, JSONArray(value.map {
                    if (it is Map<*, *>) mapToJson(it as Map<String, Any>) else it
                }))
                else -> json.put(key, value)
            }
        }
        return json
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        json.keys().forEach { key ->
            map[key] = when (val value = json.get(key)) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> (0 until value.length()).map { value.get(it) }
                JSONObject.NULL -> null
                else -> value
            }
        }
        return map
    }
}
