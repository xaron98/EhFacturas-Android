// app/src/main/java/es/ehfacturas/ai/GeminiNanoProvider.kt
package es.ehfacturas.ai

import android.content.Context
// TODO: Descomentar cuando el SDK de Google AI Core esté disponible
// import com.google.android.gms.ai.generativeai.GenerativeModel
// import com.google.android.gms.ai.generativeai.type.Content
// import com.google.android.gms.ai.generativeai.type.GenerateContentResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiNanoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : AIProvider {

    override val tipo = AIProviderType.GEMMA  // Reusar enum existente; actualizar si se añade GEMINI_NANO
    // TODO: Reemplazar con GenerativeModel real cuando el SDK esté disponible
    private var model: Any? = null
    private var disponible: Boolean? = null

    override val isAvailable: Boolean
        get() {
            if (disponible == null) {
                disponible = try {
                    // TODO: Comprobar disponibilidad real con GenerativeModel.isAvailable(context)
                    // Por ahora retornamos false hasta integrar el SDK
                    false
                } catch (_: Exception) {
                    false
                }
            }
            return disponible == true
        }

    override val unavailableReason: String
        get() = "Gemini Nano no disponible en este dispositivo"

    // TODO: Reemplazar con GenerativeModel.getModel(context, "gemini-nano") cuando el SDK esté disponible
    // private fun getOrCreateModel(): GenerativeModel {
    //     return model ?: GenerativeModel.getModel(context, "gemini-nano").also { model = it }
    // }

    override suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        return try {
            // Build prompt with system instructions and tool definitions
            val fullPrompt = buildString {
                appendLine(systemPrompt)
                appendLine()
                appendLine("HERRAMIENTAS DISPONIBLES (responde en JSON):")
                tools.forEach { tool ->
                    appendLine("- ${tool["name"]}: ${tool["description"]}")
                }
                appendLine()
                appendLine("Si necesitas usar una herramienta, responde SOLO con JSON:")
                appendLine("""{"tool": "nombre_herramienta", "arguments": {...}}""")
                appendLine("Si no necesitas herramienta, responde en texto natural.")
                appendLine()
                appendLine("Usuario: $texto")
            }

            // TODO: Llamar al modelo real cuando el SDK esté disponible:
            // val generativeModel = getOrCreateModel()
            // val response = generativeModel.generateContent(
            //     Content.Builder().addText(fullPrompt).build()
            // )
            // val respuesta = response.text ?: "Sin respuesta"
            // parseToolCallOrText(respuesta, onToolCall)

            // Placeholder hasta que el SDK esté integrado
            AICommandResult(
                texto = "Gemini Nano no disponible todavía. Usa un proveedor cloud.",
                exito = false
            )
        } catch (e: Exception) {
            AICommandResult(
                texto = "Error de Gemini Nano: ${e.message}",
                exito = false
            )
        }
    }

    /**
     * Intenta parsear la respuesta como una llamada a herramienta JSON.
     * Si no es JSON válido con "tool", lo trata como texto natural.
     */
    internal suspend fun parseToolCallOrText(
        respuesta: String,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        // Intentar parsear como JSON tool call
        val trimmed = respuesta.trim()
        if (trimmed.startsWith("{") && trimmed.contains("\"tool\"")) {
            try {
                val json = org.json.JSONObject(trimmed)
                val toolName = json.getString("tool")
                val args = json.optJSONObject("arguments") ?: org.json.JSONObject()
                val argsMap = mutableMapOf<String, Any?>()
                args.keys().forEach { key -> argsMap[key] = args.get(key) }

                val resultado = onToolCall(toolName, argsMap)
                return AICommandResult(texto = resultado, toolUsed = toolName)
            } catch (_: Exception) {
                // No es JSON válido, tratar como texto
            }
        }
        return AICommandResult(texto = respuesta)
    }

    override fun resetSession() {
        model = null
    }
}
