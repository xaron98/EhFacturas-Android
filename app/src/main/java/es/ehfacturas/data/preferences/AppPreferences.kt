package es.ehfacturas.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETADO = booleanPreferencesKey("onboarding_completado")
        val ULTIMO_NEGOCIO_ID = longPreferencesKey("ultimo_negocio_id")
        val TEMA_APP = stringPreferencesKey("tema_app")
        val IDIOMA = stringPreferencesKey("idioma")
        val PRIMERA_VEZ = booleanPreferencesKey("primera_vez")
        val CLOUD_PROVIDER = stringPreferencesKey("cloud_provider")
    }

    val onboardingCompletado: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_COMPLETADO] ?: false }

    suspend fun setOnboardingCompletado(completado: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETADO] = completado }
    }

    val ultimoNegocioId: Flow<Long> = context.dataStore.data
        .map { it[Keys.ULTIMO_NEGOCIO_ID] ?: 0L }

    suspend fun setUltimoNegocioId(id: Long) {
        context.dataStore.edit { it[Keys.ULTIMO_NEGOCIO_ID] = id }
    }

    val temaApp: Flow<String> = context.dataStore.data
        .map { it[Keys.TEMA_APP] ?: "auto" }

    suspend fun setTemaApp(tema: String) {
        context.dataStore.edit { it[Keys.TEMA_APP] = tema }
    }

    val primeraVez: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.PRIMERA_VEZ] ?: true }

    suspend fun setPrimeraVez(primeraVez: Boolean) {
        context.dataStore.edit { it[Keys.PRIMERA_VEZ] = primeraVez }
    }

    val cloudProvider: Flow<String> = context.dataStore.data
        .map { it[Keys.CLOUD_PROVIDER] ?: "claude" }

    suspend fun setCloudProvider(provider: String) {
        context.dataStore.edit { it[Keys.CLOUD_PROVIDER] = provider }
    }
}
