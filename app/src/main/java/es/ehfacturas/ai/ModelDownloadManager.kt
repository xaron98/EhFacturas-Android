package es.ehfacturas.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class DescargaEstado(
    val descargando: Boolean = false,
    val progreso: Float = 0f,
    val totalBytes: Long = 0,
    val descargadoBytes: Long = 0,
    val error: String? = null,
    val completado: Boolean = false
)

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _estado = MutableStateFlow(DescargaEstado())
    val estado: StateFlow<DescargaEstado> = _estado.asStateFlow()

    private val modelsDir = File(context.filesDir, "models")
    val modelPath: String get() = File(modelsDir, "gemma3-4b.bin").absolutePath

    val isModeloDescargado: Boolean
        get() = File(modelPath).exists()

    suspend fun descargarModelo(url: String) = withContext(Dispatchers.IO) {
        if (isModeloDescargado) {
            _estado.value = DescargaEstado(completado = true)
            return@withContext
        }

        modelsDir.mkdirs()
        _estado.value = DescargaEstado(descargando = true)

        try {
            val connection = java.net.URL(url).openConnection()
            val totalBytes = connection.contentLengthLong
            val archivo = File(modelPath)

            connection.getInputStream().use { input ->
                archivo.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var descargado = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        descargado += bytesRead
                        val progreso = if (totalBytes > 0) descargado.toFloat() / totalBytes else 0f
                        _estado.value = DescargaEstado(
                            descargando = true,
                            progreso = progreso,
                            totalBytes = totalBytes,
                            descargadoBytes = descargado
                        )
                    }
                }
            }

            _estado.value = DescargaEstado(completado = true)
        } catch (e: Exception) {
            _estado.value = DescargaEstado(error = e.message)
        }
    }

    fun cancelar() {
        _estado.value = DescargaEstado()
    }
}
