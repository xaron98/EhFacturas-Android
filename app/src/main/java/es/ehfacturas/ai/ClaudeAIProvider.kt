package es.ehfacturas.ai

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ClaudeAIProvider @Inject constructor(
    private val apiKeyManager: APIKeyManager
) : AIProvider {

    companion object {
        private const val MODEL = "claude-haiku-4-5-20251001"
        private const val MAX_TOKENS = 1024
        private const val MAX_TOOL_ITERATIONS = 3
    }

    override val tipo = AIProviderType.CLAUDE
    override val isAvailable: Boolean get() = apiKeyManager.isAuthenticated || true // Dev mode
    override val unavailableReason = "Se necesita suscripción Pro para usar Claude"

    private val conversationHistory = mutableListOf<JSONObject>()

    override suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        // Añadir mensaje del usuario
        conversationHistory.add(JSONObject().apply {
            put("role", "user")
            put("content", texto)
        })

        var lastToolUsed: String? = null
        var iterations = 0

        while (iterations < MAX_TOOL_ITERATIONS) {
            iterations++

            val body = JSONObject().apply {
                put("model", MODEL)
                put("max_tokens", MAX_TOKENS)
                put("system", systemPrompt)
                put("messages", JSONArray(conversationHistory.map { it.toString() }.map { JSONObject(it) }))
                if (tools.isNotEmpty()) {
                    put("tools", JSONArray(tools.map { mapToJson(it) }))
                }
            }

            val response = apiKeyManager.sendRequest("claude", body)

            val content = response.getJSONArray("content")
            val stopReason = response.optString("stop_reason", "end_turn")

            // Construir respuesta del asistente
            val assistantMessage = JSONObject().apply {
                put("role", "assistant")
                put("content", content)
            }
            conversationHistory.add(assistantMessage)

            if (stopReason == "tool_use") {
                // Procesar tool calls
                val toolResults = JSONArray()
                for (i in 0 until content.length()) {
                    val block = content.getJSONObject(i)
                    if (block.getString("type") == "tool_use") {
                        val toolName = block.getString("name")
                        val toolId = block.getString("id")
                        val toolInput = jsonObjectToMap(block.getJSONObject("input"))

                        lastToolUsed = toolName
                        val result = onToolCall(toolName, toolInput)

                        toolResults.put(JSONObject().apply {
                            put("type", "tool_result")
                            put("tool_use_id", toolId)
                            put("content", result)
                        })
                    }
                }

                // Añadir resultados de tools
                conversationHistory.add(JSONObject().apply {
                    put("role", "user")
                    put("content", toolResults)
                })
            } else {
                // Respuesta final de texto
                val textoRespuesta = StringBuilder()
                for (i in 0 until content.length()) {
                    val block = content.getJSONObject(i)
                    if (block.getString("type") == "text") {
                        textoRespuesta.append(block.getString("text"))
                    }
                }
                return AICommandResult(
                    texto = textoRespuesta.toString(),
                    toolUsed = lastToolUsed
                )
            }
        }

        return AICommandResult(
            texto = "Se alcanzó el límite de iteraciones de herramientas.",
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
