package es.ehfacturas.ui.importador

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.ehfacturas.domain.importador.CSVParser
import es.ehfacturas.domain.importador.ImportadorService
import es.ehfacturas.domain.importador.MapeoUniversal
import es.ehfacturas.domain.importador.ResultadoCSV
import es.ehfacturas.domain.importador.ResultadoImportacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ImportadorUiState(
    val paso: Int = 0,  // 0=seleccionar, 1=preview, 2=importando, 3=resultado
    val tipo: String = "articulos",  // "articulos" | "clientes"
    val csv: ResultadoCSV? = null,
    val mapeo: Map<String, Int> = emptyMap(),
    val confianza: Double = 0.0,
    val importando: Boolean = false,
    val resultado: ResultadoImportacion? = null,
    val error: String? = null
)

@HiltViewModel
class ImportadorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importadorService: ImportadorService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportadorUiState())
    val uiState: StateFlow<ImportadorUiState> = _uiState.asStateFlow()

    fun setTipo(tipo: String) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    fun cargarCSV(uri: Uri) {
        viewModelScope.launch {
            try {
                val csv = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { CSVParser.parsear(it) }
                } ?: return@launch

                val resultado = MapeoUniversal.detectar(csv.cabeceras, _uiState.value.tipo)

                _uiState.update {
                    it.copy(
                        paso = 1,
                        csv = csv,
                        mapeo = resultado.mapeo,
                        confianza = resultado.confianza,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error leyendo CSV: ${e.message}") }
            }
        }
    }

    fun actualizarMapeo(campo: String, indice: Int) {
        _uiState.update { it.copy(mapeo = it.mapeo + (campo to indice)) }
    }

    fun importar() {
        val state = _uiState.value
        val csv = state.csv ?: return

        _uiState.update { it.copy(paso = 2, importando = true) }

        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                if (state.tipo == "articulos") {
                    importadorService.importarArticulos(csv.filas, state.mapeo)
                } else {
                    importadorService.importarClientes(csv.filas, state.mapeo)
                }
            }
            _uiState.update { it.copy(paso = 3, importando = false, resultado = resultado) }
        }
    }

    fun reiniciar() {
        _uiState.value = ImportadorUiState()
    }
}
