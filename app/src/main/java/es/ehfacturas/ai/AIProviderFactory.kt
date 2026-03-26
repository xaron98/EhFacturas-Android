package es.ehfacturas.ai

import es.ehfacturas.data.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProviderFactory @Inject constructor(
    private val appPreferences: AppPreferences,
    private val geminiNanoProvider: GeminiNanoProvider,
    private val gemmaLocalProvider: GemmaLocalProvider,
    private val claudeProvider: ClaudeAIProvider,
    private val openaiProvider: OpenAIProvider
) {
    suspend fun getProvider(): AIProvider {
        if (geminiNanoProvider.isAvailable) return geminiNanoProvider
        if (gemmaLocalProvider.isAvailable) return gemmaLocalProvider
        return getCloudProvider()
    }

    suspend fun getCloudProvider(): AIProvider {
        val preferred = appPreferences.cloudProvider.first()
        return when (preferred) {
            "claude" -> if (claudeProvider.isAvailable) claudeProvider else
                        if (openaiProvider.isAvailable) openaiProvider else UnavailableAIProvider()
            "openai" -> if (openaiProvider.isAvailable) openaiProvider else
                         if (claudeProvider.isAvailable) claudeProvider else UnavailableAIProvider()
            else -> if (claudeProvider.isAvailable) claudeProvider else UnavailableAIProvider()
        }
    }

    fun resetProvider() {
        geminiNanoProvider.resetSession()
        gemmaLocalProvider.resetSession()
        claudeProvider.resetSession()
        openaiProvider.resetSession()
    }
}
