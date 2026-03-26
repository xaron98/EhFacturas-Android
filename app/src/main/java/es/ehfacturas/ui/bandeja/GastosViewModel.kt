package es.ehfacturas.ui.bandeja

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.data.db.entity.Gasto
import es.ehfacturas.data.repository.GastoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GastosViewModel @Inject constructor(
    private val gastoRepository: GastoRepository
) : ViewModel() {

    val gastos: StateFlow<List<Gasto>> = gastoRepository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria: StateFlow<String?> = _filtroCategoria.asStateFlow()

    val gastosFiltrados: StateFlow<List<Gasto>> = combine(
        gastos, _filtroCategoria
    ) { lista, cat ->
        if (cat == null) lista else lista.filter { it.categoria == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mostrarFormulario = MutableStateFlow(false)
    val mostrarFormulario: StateFlow<Boolean> = _mostrarFormulario.asStateFlow()

    private val _gastoSeleccionado = MutableStateFlow<Gasto?>(null)
    val gastoSeleccionado: StateFlow<Gasto?> = _gastoSeleccionado.asStateFlow()

    // Total del mes actual
    private val inicioMes: Date
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

    private val finMes: Date
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            return cal.time
        }

    val totalMes: StateFlow<Double> = gastoRepository
        .totalPeriodo(inicioMes, finMes)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun filtrarPorCategoria(categoria: String?) {
        _filtroCategoria.value = categoria
    }

    fun mostrarFormulario(gasto: Gasto? = null) {
        _gastoSeleccionado.value = gasto
        _mostrarFormulario.value = true
    }

    fun cerrarFormulario() {
        _mostrarFormulario.value = false
        _gastoSeleccionado.value = null
    }

    fun guardarGasto(gasto: Gasto) {
        viewModelScope.launch {
            if (gasto.id == 0L) {
                gastoRepository.guardar(gasto)
            } else {
                gastoRepository.actualizar(gasto)
            }
            cerrarFormulario()
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            gastoRepository.eliminar(gasto)
        }
    }
}
