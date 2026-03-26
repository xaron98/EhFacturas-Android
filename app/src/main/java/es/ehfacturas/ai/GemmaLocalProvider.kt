package es.ehfacturas.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaLocalProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: ModelDownloadManager
) : AIProvider {

    override val tipo = AIProviderType.GEMMA
    private var inference: LlmInference? = null

    override val isAvailable: Boolean
        get() = downloadManager.isModeloDescargado

    override val unavailableReason: String
        get() = "Modelo Gemma no descargado"

    private fun getOrCreateInference(): LlmInference {
        return inference ?: LlmInference.createFromOptions(
            context,
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(downloadManager.modelPath)
                .setMaxTokens(1024)
                .build()
        ).also { inference = it }
    }

    override suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        return try {
            val llm = getOrCreateInference()

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

            val respuesta = llm.generateResponse(fullPrompt)

            // Parse tool call or text
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
                } catch (_: Exception) { }
            }

            AICommandResult(texto = respuesta)
        } catch (e: Exception) {
            AICommandResult(texto = "Error de Gemma: ${e.message}", exito = false)
        }
    }

    override fun resetSession() {
        inference?.close()
        inference = null
    }
}
