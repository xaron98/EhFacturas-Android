package es.ehfacturas.ai

// Resultado de un comando de IA
data class AICommandResult(
    val texto: String,
    val toolUsed: String? = null,
    val exito: Boolean = true
)

// Tipo de proveedor de IA
enum class AIProviderType(val descripcion: String) {
    GEMMA("Gemma 2B (on-device)"),
    CLAUDE("Claude (cloud)"),
    OPENAI("OpenAI (cloud)"),
    NONE("Sin IA disponible")
}

// Modo de operación
enum class AIMode {
    COMMAND,  // Modo comando (12 tools)
    EDIT      // Modo edición de factura (4 tools)
}

// Interfaz base para proveedores de IA
interface AIProvider {
    val tipo: AIProviderType
    val isAvailable: Boolean
    val unavailableReason: String

    suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult

    fun resetSession()
}

// Proveedor stub para cuando no hay IA disponible
class UnavailableAIProvider : AIProvider {
    override val tipo = AIProviderType.NONE
    override val isAvailable = false
    override val unavailableReason = "No hay proveedor de IA configurado"

    override suspend fun processCommand(
        texto: String,
        systemPrompt: String,
        tools: List<Map<String, Any>>,
        onToolCall: suspend (String, Map<String, Any?>) -> String
    ): AICommandResult {
        return AICommandResult(
            texto = "No hay proveedor de IA disponible. Configura Claude o OpenAI en Ajustes.",
            exito = false
        )
    }

    override fun resetSession() {}
}
