package es.ehfacturas.ui.plantillas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.ai.ToolExecutor
import es.ehfacturas.data.db.dao.PlantillaDao
import es.ehfacturas.data.db.entity.PlantillaFactura
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantillasViewModel @Inject constructor(
    private val plantillaDao: PlantillaDao,
    private val toolExecutor: ToolExecutor
) : ViewModel() {

    val plantillas: StateFlow<List<PlantillaFactura>> = plantillaDao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Resultado de la última acción de usar plantilla */
    private val _resultadoUso = MutableStateFlow<String?>(null)
    val resultadoUso: StateFlow<String?> = _resultadoUso.asStateFlow()

    /**
     * Usa la plantilla para crear una factura borrador a través de ToolExecutor.
     * Incrementa el contador vecesUsada.
     */
    fun usarPlantilla(plantilla: PlantillaFactura) {
        viewModelScope.launch {
            val resultado = toolExecutor.executeTool(
                "crear_factura",
                mapOf("articulosTexto" to plantilla.articulosTexto)
            )
            // Incrementar contador de uso
            plantillaDao.actualizar(plantilla.copy(vecesUsada = plantilla.vecesUsada + 1))
            _resultadoUso.value = resultado
        }
    }

    fun limpiarResultado() {
        _resultadoUso.value = null
    }

    fun eliminar(plantilla: PlantillaFactura) {
        viewModelScope.launch {
            plantillaDao.eliminar(plantilla)
        }
    }
}
