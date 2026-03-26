package es.ehfacturas.ai

import es.ehfacturas.data.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIProviderFactory @Inject constructor(
    private val appPreferences: AppPreferences,
    private val claudeProvider: ClaudeAIProvider,
    private val openaiProvider: OpenAIProvider
) {
    private var cachedProvider: AIProvider? = null

    suspend fun getProvider(): AIProvider {
        cachedProvider?.let { return it }

        val cloudProvider = appPreferences.cloudProvider.first()

        val provider = when (cloudProvider) {
            "claude" -> if (claudeProvider.isAvailable) claudeProvider else UnavailableAIProvider()
            "openai" -> if (openaiProvider.isAvailable) openaiProvider else UnavailableAIProvider()
            else -> UnavailableAIProvider()
        }

        cachedProvider = provider
        return provider
    }

    fun resetProvider() {
        cachedProvider?.resetSession()
        cachedProvider = null
    }
}
